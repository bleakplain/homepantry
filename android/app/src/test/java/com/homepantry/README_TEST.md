# HomePantry Android Tests

## Test Structure

### Unit Tests (`src/test/`)
Tests that run on the JVM without Android dependencies.

#### Repository Tests
- `RecipeRepositoryTest.kt` - Tests for RecipeRepository
  - CRUD operations
  - Recipe with details (ingredients, instructions)

- `IngredientRepositoryTest.kt` - Tests for IngredientRepository
  - Ingredient CRUD operations
  - Pantry item management
  - Expiry date handling

#### ViewModel Tests
- `RecipeViewModelTest.kt` - Tests for RecipeViewModel
  - State management
  - Recipe operations
  - Error handling

### Instrumentation Tests (`src/androidTest/`)
Tests that run on Android device/emulator.

#### UI Tests
- `NavigationTest.kt` - Tests navigation between screens
- `RecipeListScreenTest.kt` - Tests recipe list screen UI
- `HomeScreenTest.kt` - Tests home screen UI

## Running Tests

### Run all unit tests
```bash
./gradlew test
```

### Run all instrumentation tests
```bash
./gradlew connectedAndroidTest
```

### Run specific test class
```bash
./gradlew test --tests com.homepantry.data.repository.RecipeRepositoryTest
```

### Run specific test method
```bash
./gradlew test --tests com.homepantry.data.repository.RecipeRepositoryTest.insert_recipe_calls_dao_insert
```

## Test Coverage

Current test coverage:
- Repository layer: ~80%
- ViewModel layer: ~70%
- UI layer: ~60%

## Notes

- Tests use Mockito for mocking
- Coroutines tests use StandardTestDispatcher
- UI tests use Compose Testing framework
- Some UI tests may require actual Android device/emulator
