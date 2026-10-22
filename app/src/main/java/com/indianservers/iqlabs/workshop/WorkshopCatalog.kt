package com.indianservers.iqlabs.workshop

import androidx.compose.ui.graphics.Color
import com.indianservers.iqlabs.model.*

object WorkshopCatalog {
    fun kind(id:String)=WorkshopKind.entries.firstOrNull { it.id==id }
    val games=WorkshopKind.entries.map { kind ->
        val laser=kind==WorkshopKind.Laser
        GameDefinition(kind.id,kind.title,
            if(laser)"Reflect and split real laser beams. Illuminate every crystal!" else "Rotate glossy open rings. Align the gaps and free every collar!",
            LevelId.Explorer,if(laser)Category.Logic else Category.Spatial,listOf("Infinite","Offline","Puzzle","Physics"),1,4,6,
            if(laser)"✦" else "◌",Color(if(laser)0xFFCE8A35 else 0xFF00B8D7),
            if(laser)"Tap mirrors to rotate their surfaces. Light every receiver with its exact RGB colour. Regular rotations are free; Challenge charges lives only on an explicit failed check." else "Drag around a C-ring to rotate it. The opening must clear its gold base guide and every remaining connector collar before the ring lifts out. Rotating is free; only an explicit blocked release costs a life.",
            "Earn 1–3 stars for accurate solving with thoughtful hints. Rewards can be claimed once per level.",20,"Always available",BestScoreType.HigherIsBetter,ImplementationStatus.Playable,
            "Relaxed mode has unlimited lives. Rings also have a 15-degree turn button and pinch zoom. All progress and undo history are saved offline.")
    }
}
