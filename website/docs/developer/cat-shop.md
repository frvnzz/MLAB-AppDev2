# Cat Shop

The Cat Shop allows users to spend the currency they've earned through goal tracking sessions 
to adopt new cats. These cats can then be displayed in the user's room on `HomeScreen`.

## Main files

- `ShopScreen.kt`: The main grid view displaying all available cats.
- `ShopItemCard.kt`: A component representing a single cat in the shop, showing its image, price, 
  and ownership status.
- `ShopService.kt`: Business logic for purchasing cats and managing the user's collection.
- `CatList.kt`: The static registry of all available cats in the game.
- `UserViewModel.kt`: Provides the user's current balance and collection state to the UI.
- `User.kt`: Domain model storing the user's `balance`, `collectedCatsIds`, and `selectedCatIds`.

## Core Functionality

### 1. Item Display
The shop retrieves the full list of cats from `CatList.kt`. For each cat, the `ShopScreen` 
determines:
- **Ownership**: If the cat's ID exists in the user's `collectedCatsIds`.
- **Affordability**: If the user's `balance` is greater than or equal to the cat's `price`.

### 2. Adoption Logic
When a user clicks "Adopt", the `ShopService.buyCat()` function is triggered:
- **Validation**: It verifies that the user doesn't already own the cat and has sufficient funds.
- **Transaction**: The cat's price is deducted from the user's balance.
- **Collection**: The cat's ID is added to `collectedCatsIds`.
- **Auto-Selection**: If the user has fewer than 5 cats currently selected for their room, 
   the newly adopted cat is automatically added to `selectedCatIds` so it appears immediately in 
   the `RoomView`.

### 3. UI States
The `ShopItemCard` reflects the purchase status dynamically:
- **Adopt**: Enabled if the cat is not owned and funds are sufficient.
- **Owned**: Displayed (and disabled) if the cat is already in the user's collection.
- **No Funds**: Displayed (and disabled) if the user cannot afford the cat.

## Notes

- **Static Data**: Cat definitions (names, prices, images) are stored in code (`CatList`) rather 
  than a database, while ownership and balance are persisted via Room.
- **Currency Sync**: The shop uses the same balance updated by the `TrackingService`, creating a 
  tight loop between productivity and rewards.
