# Time Tracking & Rewards

The Time Tracking feature allows users to record productivity sessions for their selected goals and 
earn rewards (currency) based on the session duration.

## Main files

- `TrackingScreen.kt`: Displays the active timer and the final reward summary.
- `TrackingSession.kt`: Domain model representing a single tracking period.
- `TrackingViewModel.kt`: Manages the timer state, session persistence, and reward flow.
- `TrackingService.kt`: Business logic for starting/stopping sessions and calculating rewards.
- `TrackingRepository.kt`: Handles database operations for `TrackingSession` entities.
- `RewardService.kt`: Contains the logic for calculating currency rewards and multipliers.
- `TrackingDao`: Room DAO defining the database queries for the `TrackingSessionEntity`.

## Starting a Tracking Session

Sessions are initiated from the `HomeScreen` via the `GoalBottomDrawer`. 

1. The user selects a goal from the drawer list (persisted in `SharedPreferences`).
2. Clicking the **Play** button triggers `handleStartTrackingClick`.
3. If the goal has `deepFocus` enabled but accessibility is not set up, a dialog prompts the user 
   to enable it.
4. Otherwise, `TrackingViewModel.startTrack()` is called, which creates a new session in the 
   database and navigates the user to the `TrackingScreen`.

## Rewards Calculation

Rewards are calculated in `RewardService` when a session is stopped. The logic ensures that 
longer, consistent focus sessions are rewarded more heavily.

- **Base Reward**: 1 coin per tracked minute.
- **Multiplier**: 
    - Sessions under 15 minutes have a **1.0x** multiplier.
    - At 15 minutes, the multiplier starts at **1.15x**.
    - For every additional 15 minutes, the multiplier increases by **0.10x**.
    - The maximum possible multiplier is **2.0x**.
- **Total Coins**: `round(minutes * multiplier)`

## Runtime Flow

1. **Start**: `TrackingService` inserts a `TrackingSession` with a `startTime` and `endTime = null`.
2. **Active**: `TrackingViewModel` runs a coroutine-based ticker that updates the `elapsedMillis` 
   in the UI every second.
3. **Stop**: `TrackingService` updates the session with an `endTime`. It then calls 
   `RewardService` to determine the reward and updates the user's balance in `UserRepository`.
4. **Summary**: The `TrackingScreen` switches from the timer view to a reward summary, 
   displaying the coins earned and the multiplier achieved.
5. **Completion**: The user clicks "Return Home" to reset the tracking state and return to 
   the main dashboard.
