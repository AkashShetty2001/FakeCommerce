# Using MapStruct to Reduce DTO Mapping Boilerplate

> Status: reference guide only. No code in the app has been changed — this
> is meant to be learned and applied one entity at a time, starting with
> `Category` as the example below.

## The problem it solves

Right now, mapping between entities and DTOs is written by hand in every
service. For example, `CategoryService.createCategory` builds a `Category`
from a `CreateCategoryRequestDto` manually:

```java
public Category createCategory(CreateCategoryRequestDto categoryRequestDto){
    Category newCategory = Category.builder()
            .categoryName(categoryRequestDto.getCategoryName())
            .build();
    return categoryRepository.save(newCategory);
}
```

This is fine for a DTO with one field, but as entities grow (more fields,
nested objects, renamed fields between the DTO and entity) this hand-written
mapping code:

- gets repeated across `create`, `update`, and `toResponseDto` style methods
- is easy to forget to update when a field is added to the entity
- adds noise to the service layer that has nothing to do with business logic

MapStruct is an annotation processor that generates this mapping code for
you at **compile time**. You write an interface describing the mapping,
and MapStruct generates a plain Java implementation — no reflection, no
runtime cost, and the generated code is fully debuggable.

## How it works, in short

1. You declare a `@Mapper` interface with abstract methods like
   `Category toEntity(CreateCategoryRequestDto dto)`.
2. During the Gradle/Maven build, the MapStruct annotation processor scans
   these interfaces and generates an implementation class
   (`CategoryMapperImpl`) in `build/generated/sources/annotationProcessor`.
3. If `componentModel = "spring"` is set, the generated impl is annotated
   `@Component`, so it can be `@Autowired`/constructor-injected like any
   other Spring bean.

## Setup (Gradle)

The project already uses Lombok's annotation processor
(`build.gradle:23-31`). MapStruct also runs as an annotation processor, and
when both are used together, Lombok must run *before* MapStruct so
MapStruct can see the generated getters/builders. That ordering is handled
by an extra binding dependency:

```groovy
dependencies {
    implementation 'org.mapstruct:mapstruct:1.6.3'
    annotationProcessor 'org.mapstruct:mapstruct-processor:1.6.3'

    // required so MapStruct sees Lombok-generated getters/builders
    annotationProcessor 'org.projectlombok:lombok-mapstruct-binding:0.2.0'
}
```

(Not added to `build.gradle` yet — add this when you're ready to implement
it for real.)

## Example: mapping `Category`

Using the existing `Category` entity and `CreateCategoryRequestDto`
(`src/main/java/com/fakecommerce/schema/Category.java` and
`src/main/java/com/fakecommerce/dtos/CreateCategoryRequestDto.java`), plus a
response DTO you'd introduce so the controller stops returning the entity
directly:

```java
// dtos/CategoryResponseDto.java
package com.fakecommerce.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryResponseDto {
    private Long id;
    private String categoryName;
}
```

```java
// mappers/CategoryMapper.java
package com.fakecommerce.mappers;

import com.fakecommerce.dtos.CategoryResponseDto;
import com.fakecommerce.dtos.CreateCategoryRequestDto;
import com.fakecommerce.schema.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toEntity(CreateCategoryRequestDto dto);

    CategoryResponseDto toResponseDto(Category category);
}
```

That's it — no method bodies. Since every field name matches
(`categoryName` -> `categoryName`, `id` -> `id`), MapStruct generates the
mapping automatically. For a field name mismatch you'd add
`@Mapping(source = "...", target = "...")` above the method.

MapStruct generates roughly this at build time
(`build/generated/sources/annotationProcessor/.../CategoryMapperImpl.java`):

```java
@Component
public class CategoryMapperImpl implements CategoryMapper {
    @Override
    public Category toEntity(CreateCategoryRequestDto dto) {
        if (dto == null) return null;
        return Category.builder()
                .categoryName(dto.getCategoryName())
                .build();
    }

    @Override
    public CategoryResponseDto toResponseDto(Category category) {
        if (category == null) return null;
        return CategoryResponseDto.builder()
                .id(category.getId())
                .categoryName(category.getCategoryName())
                .build();
    }
}
```

You never write or maintain that file — it's regenerated on every build.

### Using it in `CategoryService`

The mapper is just another constructor-injected bean, consistent with the
`@RequiredArgsConstructor` pattern already used everywhere in this project:

```java
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryResponseDto createCategory(CreateCategoryRequestDto requestDto) {
        Category newCategory = categoryMapper.toEntity(requestDto);
        Category saved = categoryRepository.save(newCategory);
        return categoryMapper.toResponseDto(saved);
    }
}
```

The manual `Category.builder()...build()` and any future manual
`CategoryResponseDto.builder()...build()` blocks disappear from the service
— the service is left with only persistence + business logic.

## Applying this to other entities

The pattern is the same for every entity in the app (`Order`, `Product`,
etc.):

1. Add a `*ResponseDto` if one doesn't exist yet.
2. Add a `*Mapper` interface with `toEntity(...)` and `toResponseDto(...)`
   methods.
3. Inject the mapper into the service and replace manual `builder()` calls
   with `mapper.toEntity(...)` / `mapper.toResponseDto(...)`.
4. For mapping a list (e.g. `OrderService.getAllOrders`), just add
   `List<CategoryResponseDto> toResponseDtoList(List<Category> categories);`
   to the mapper — MapStruct generates the loop for you.

For entities with nested objects (like `Order` -> `OrderItems` ->
`Product`), MapStruct supports composing multiple mappers together — a
mapper can reference another `@Mapper`-annotated interface for the nested
type. Worth reading up on once the basic single-entity case is comfortable.

## Reference

- MapStruct official reference documentation (setup, `@Mapping`, nested
  mappers, Spring component model):
  https://mapstruct.org/documentation/stable/reference/html/
- Using MapStruct with Spring specifically (`componentModel = "spring"`,
  dependency injection):
  https://mapstruct.org/documentation/stable/reference/html/#using-spring
