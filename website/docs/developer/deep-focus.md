# Deep Focus

Deep Focus is implemented as a per-goal blocking flow that is activated when tracking starts and
deactivated when tracking stops.

## Main files

- `HomeScreen.kt` / `AddGoalScreen.kt` / `EditGoalScreen.kt`: expose the `deepFocus` flag on goals
  and guard start-up with the accessibility setup dialog.
- `TrackingViewModel.kt`: starts/stops tracking and toggles blocking state through the
  repository/service layer.
- `DeepFocusAccessibilityService.kt`: observes foreground window changes, filters launcher/system
  surfaces, and decides when to show or remove the overlay.
- `DeepFocusOverlayController.kt`: builds and removes the accessibility overlay UI.
- `DeepFocusConfig.kt`: stores the shared preference key that enables/disables blocking.

## Runtime flow

1. A goal is saved with `deepFocus = true`.
2. Starting tracking enables the blocking flag.
3. The accessibility service reacts to `TYPE_WINDOW_STATE_CHANGED` events and shows the overlay for
   non-allowed apps.
4. The overlay’s return action launches `Purrsistence` again and uses a short grace window to avoid
   immediate re-attach.
5. `stopTracking()` clears blocking and removes any active overlay.

## Notes

- Android does not provide a built-in API for fully disabling an app; Deep Focus uses accessibility
  services to display an overlay instead.
- This is different from iOS, where Screen Time provides a dedicated API for app restriction.
- Accessibility must be enabled manually in Android Settings before Deep Focus can block apps.
- Launcher and system UI packages are intentionally excluded from blocking to avoid trapping the
  user.
