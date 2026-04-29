# Isometric Room

The Isometric Room is a gamified visualization on the `HomeScreen` where users can see their 
collected cats. It serves as the primary reward feedback loop, showing the tangible results of 
the user's productivity.

## Main files

- `HomeScreen.kt`: The main entry point that manages the room's state and cat selection.
- `RoomView.kt`: The core component that renders the isometric environment and positions cats.
- `RoomService.kt`: Contains the coordinates for room spots and logic for placing cats.
- `CatSelectionDialog.kt`: A UI dialog for users to choose which 5 cats to display in the room.
- `CatImage.kt`: Renders individual cat sprites with support for mirroring.
- `RoomSpot.kt` / `PlacedCat.kt`: Models representing coordinate points and occupied spots.

## Coordinate System & Cat Placement Logic
The room uses a percentage-based coordinate system defined in `RoomSpot`.
- **Responsive Positioning**: Cats are positioned using `xPercent` and `yPercent` relative to the 
  background image. `RoomView` calculates the actual pixel offsets based on the container's size 
  to ensure cats stay on the floor regardless of the device's aspect ratio.
- **Isometric Z-Index**: To maintain the 3D illusion, the `zIndex` of each cat is set to its 
  `yPercent`. This ensures that cats positioned "higher" on the screen (further back) are 
  rendered behind cats that are "lower" (closer to the front).

The `RoomService.assignCatsToSpots()` function handles how cats are distributed:
- **Randomization**: To make the room feel dynamic, cats are assigned to spots in a shuffled order 
  each time the screen is loaded.
- **Mirroring**: Cats have a random chance to be mirrored (`scaleX = -1f`), adding variety to the 
  visuals even if the user has few cats.

## Collection Management
While a user may own many cats, the room is limited to **5 active spots**:
- **Auto-Selection**: New cats are automatically added to the room upon purchase if there is space.
- **Manual Selection**: Users can click the icon button on the `HomeScreen` to open the 
  `CatSelectionDialog`, where they can manually toggle which of their `collectedCatsIds` should be 
  mapped to `selectedCatIds` of the `User` entity.

## Notes

- **Asset Alignment**: The `RoomView` includes a debug log in its `pointerInput` that prints 
  percentage coordinates when the screen is tapped. This is used during development to easily 
  find new `RoomSpot` coordinates for the background image.
- **Z-Order**: The background room image is rendered first, and cats are drawn on top using 
  absolute offsets calculated from the `BoxWithConstraints` dimensions.
