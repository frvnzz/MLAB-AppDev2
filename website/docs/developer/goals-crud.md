# Goals (CRUD)

The Goals feature allows users to create, view, update, and delete their productivity goals. Each goal
is defined by a title, a category (type), a target duration, and a "Deep Focus" toggle.

## Main files

- `Goal.kt`: Core domain model representing a goal.
- `GoalsScreen.kt`: The primary list view where users can see, search, and manage their goals.
- `AddGoalScreen.kt` / `EditGoalScreen.kt`: Dedicated screens for creating and modifying goals.
- `GoalViewModel.kt`: Orchestrates UI state and interacts with the service layer.
- `GoalService.kt`: Business logic layer for goal-related operations.
- `GoalRepositoryImpl.kt`: Contains both the `GoalRepository` interface and its implementation for data persistence.
- `GoalsDao.kt`: Room DAO defining the database queries for the `GoalEntity`.

## CRUD Operations

- **Create**: Users add new goals via `AddGoalScreen`. The `GoalViewModel.addGoal()` function
  persists the new goal with a default `inactive = false` state and the current timestamp.
- **Read**: Goals are retrieved as a `Flow` to ensure the UI updates reactively. `GoalsScreen`
  utilizes `searchedGoals(userId)` to provide real-time filtering as the user types in the
  search bar.
- **Update**: `EditGoalScreen` allows users to modify existing goal details. The `updateGoal()`
  function in the service layer ensures that changes are saved back to the database.
- **Delete**: Supports both single and batch deletion. In `GoalsScreen`, users can enter an "Edit"
  mode to select multiple goals and delete them simultaneously after a confirmation dialog.

## Notes

- **Search**: The search functionality is implemented at the database level using SQL `LIKE`
  patterns, ensuring efficient filtering even with many goals.
- **State Management**: The selected goal for tracking is persisted in `SharedPreferences` via
  `GoalViewModel`. Users select their active goal they want to track via the `GoalBottomDrawer` on the `HomeScreen`.
- **Data Integrity**: Deletion is guarded by `DeleteGoalDialog` to prevent accidental removal of
  goal history and associated tracking data.
- **Deep Focus Integration**: Each goal carries a `deepFocus` flag that determines if app blocking
  should be active when tracking that specific goal.