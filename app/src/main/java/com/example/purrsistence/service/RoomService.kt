package com.example.purrsistence.service

import com.example.purrsistence.domain.model.PlacedCat
import com.example.purrsistence.domain.model.RoomSpot

class RoomService {

    fun getRoomSpots(): List<RoomSpot> {
        return listOf(
            // All possible spots + coordinates where a CatImage can be rendered in RoomView
            RoomSpot("floor_center", 0.498f, 0.757f),
            RoomSpot("floor_left",   0.155f, 0.786f),
            RoomSpot("floor_right",  0.899f, 0.760f),
            RoomSpot("floor_front",  0.491f, 0.908f),
            RoomSpot("floor_back",   0.434f, 0.647f)
        )
    }

    fun assignCatsToSpots(
        ownedCatIds: List<String>,
        spots: List<RoomSpot>
    ): List<PlacedCat> {
        if (spots.isEmpty()) return emptyList()
        val shuffledSpots = spots.shuffled()
        return ownedCatIds.mapIndexed { index, catId ->
            val spot = shuffledSpots[index % spots.size]
            PlacedCat(catId, spot.id)
        }
    }
}
