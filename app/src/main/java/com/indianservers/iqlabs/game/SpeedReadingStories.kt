package com.indianservers.iqlabs.game

internal data class SpeedReadingStory(
    val passage: String,
    val question: String,
    val answer: String,
    val distractors: List<String>,
    val explanation: String,
)

internal object SpeedReadingStories {
    val entries = listOf(
        SpeedReadingStory(
            "Anika discovered a packet of bean seeds behind her grandmother's kitchen jars. She planted them in three pots near the window and marked each pot with a date. Every morning, she checked the soil before watering. When the first leaves appeared, she moved the pots onto the balcony. By summer, she had enough beans to share with the family next door.",
            "What did Anika do with the beans she grew?", "Shared them with neighbours", listOf("Sold them at school", "Fed them to birds", "Stored every bean"), "She shared her summer harvest with the family next door.",
        ),
        SpeedReadingStory(
            "During a rainy afternoon, Kabir noticed water dripping onto books in the school library. He placed a bucket beneath the leak and asked the librarian for help. Together, they moved the books to a dry table. The next morning, a worker repaired the roof. Kabir later designed shelf labels so the rescued books could return to their proper places without confusion.",
            "Why were the books moved to a table?", "To protect them from water", listOf("To prepare a book sale", "To count missing pages", "To paint the shelves"), "The leaking roof threatened to soak the books.",
        ),
        SpeedReadingStory(
            "Leela wanted to photograph the sunrise from a hill outside her town. She packed her camera the night before and set two alarms. At dawn, thick fog covered the path, so she walked slowly beside her father. They reached the top just as the fog lifted. Her favourite photograph showed a golden sky reflected in a small puddle beside their shoes.",
            "What reflected the golden sky in Leela's favourite photograph?", "A small puddle", listOf("A shop window", "Her camera lens", "A silver watch"), "The photograph captured the sky reflected in a puddle beside their shoes.",
        ),
        SpeedReadingStory(
            "The village bus stopped when a fallen branch blocked the narrow road. Instead of complaining, passengers stepped outside to help the driver. Two farmers lifted one end while others pulled away smaller twigs. Mehul collected the loose pieces in a pile beside the fence. Within fifteen minutes, the road was clear, and the passengers continued their journey to the weekly market.",
            "Where were the passengers travelling?", "The weekly market", listOf("A mountain campsite", "The airport", "A wedding hall"), "The passengers continued their journey to the weekly market after clearing the road.",
        ),
        SpeedReadingStory(
            "Sana borrowed a telescope from her science teacher for a weekend. She expected to see distant planets immediately, but the first evening was cloudy. On Sunday, the sky cleared, and her brother helped steady the stand. They studied the bright moon and sketched its darker patches. Sana returned the telescope with a notebook describing what she had actually observed rather than what she had expected.",
            "What did Sana and her brother study through the telescope?", "The moon", listOf("A passing aeroplane", "A distant comet", "The planet Mars"), "They observed the bright moon and drew its darker patches.",
        ),
        SpeedReadingStory(
            "Rohan's bicycle made a clicking sound whenever he turned the pedals. He wanted to buy a new bicycle, but his aunt suggested checking the chain first. At the repair shop, the mechanic found a small stone trapped near a gear. Removing it stopped the noise completely. Rohan used the money he had saved to buy a helmet and a bright rear light.",
            "What caused the bicycle's clicking sound?", "A trapped stone", listOf("A flat tyre", "A loose seat", "A cracked helmet"), "The mechanic removed a stone trapped near a gear, which stopped the clicking.",
        ),
        SpeedReadingStory(
            "At the beach cleanup, Tara was assigned a stretch of sand near the fishing boats. She wore gloves and separated plastic wrappers from broken glass. Under a wooden crate, she found a tiny crab struggling inside a cup. An adult carefully released it near the rocks. Tara recorded the litter they collected and suggested placing covered bins beside the path to the boats.",
            "What did Tara suggest after the cleanup?", "Covered bins beside the path", listOf("Removing all fishing boats", "Building a new pier", "Closing the beach permanently"), "She suggested covered bins beside the path to reduce litter.",
        ),
        SpeedReadingStory(
            "Dev prepared a puppet show for his younger cousins using old socks and cardboard scenery. Minutes before the performance, one puppet lost its button eyes. He drew new eyes with a marker and changed the character into a sleepy owl. The cousins laughed at the owl's slow replies. Afterward, they asked to make their own puppets and perform a second story together.",
            "How did Dev replace the puppet's missing eyes?", "Drew them with a marker", listOf("Sewed on two coins", "Used paper clips", "Borrowed glass beads"), "Dev drew new eyes with a marker before turning the character into an owl.",
        ),
        SpeedReadingStory(
            "Mira visited a pottery workshop where everyone shaped a bowl from wet clay. Her first bowl leaned to one side, and she nearly started again. The instructor showed her how to support the wall with both hands. Mira repaired the shape and pressed a leaf pattern around the rim. Once the bowl was fired, she used it to hold keys near the front door.",
            "What did Mira keep in her finished bowl?", "Keys", listOf("Bean seeds", "Paintbrushes", "Seashells"), "The finished bowl held keys near the front door.",
        ),
        SpeedReadingStory(
            "Arjun and his friends planned a picnic under a large banyan tree. When they arrived, they found the ground muddy from overnight rain. They moved to the covered pavilion instead and spread their mats on its dry floor. A friend brought a board game, so nobody missed the grassy spot. Before leaving, they collected every wrapper and checked that the pavilion looked clean.",
            "Why did the friends move their picnic?", "The ground was muddy", listOf("The tree was too small", "They forgot their food", "The pavilion had music"), "Overnight rain had left the ground beneath the tree muddy.",
        ),
        SpeedReadingStory(
            "Nisha noticed that an elderly neighbour struggled to read the small numbers on the building's mailboxes. With permission from the residents, she printed larger labels using dark ink on white paper. Her neighbour could now find his box easily. Other residents appreciated the change as well, especially in the evening. Nisha learned that a simple improvement could make a shared space more welcoming for everyone.",
            "What made the mailboxes easier to identify?", "Larger printed labels", listOf("New metal locks", "Different box heights", "A louder doorbell"), "Nisha replaced small numbers with larger labels in dark ink on white paper.",
        ),
        SpeedReadingStory(
            "Ishan joined a cooking class and volunteered to measure ingredients for vegetable soup. He accidentally added too much water, making the soup thin. The teacher suggested simmering it longer and adding a mashed potato. Ishan stirred patiently until the texture improved. At lunch, his classmates enjoyed the soup, and he wrote the correction in his recipe notebook so he could remember it next time.",
            "What ingredient helped thicken the soup?", "A mashed potato", listOf("A spoon of sugar", "A sliced apple", "A cup of cold milk"), "The teacher suggested adding a mashed potato and simmering the soup longer.",
        ),
        SpeedReadingStory(
            "Zoya found a faded map inside a secondhand adventure book. It showed a walking route through a nearby botanical garden, with a star beside the old fountain. She followed the route with her mother on Saturday. The fountain was no longer running, but a plaque explained its history. Zoya photographed the plaque and tucked a fresh copy of the garden map into the book.",
            "Where did the star on the old map point?", "The old fountain", listOf("The ticket office", "The rose greenhouse", "The garden cafe"), "A star marked the old fountain on the walking route.",
        ),
        SpeedReadingStory(
            "Vikram's team built a bridge from drinking straws for a classroom challenge. Their first design bent under a stack of coins. After studying a photograph of a railway bridge, they added diagonal supports between the sides. The revised bridge held twice as many coins. Vikram explained that the extra supports helped spread the load, and the team displayed both designs to show their progress.",
            "What improved the team's straw bridge?", "Diagonal supports", listOf("A coat of paint", "Shorter drinking straws only", "Removing the sides"), "Diagonal supports helped spread the load and allowed the bridge to hold more coins.",
        ),
        SpeedReadingStory(
            "Pooja promised to water her friend's plants while the family travelled. She found a note explaining that the cactus needed much less water than the fern. Rather than watering every pot equally, she followed the instructions and checked the soil each visit. When her friend returned, the plants looked healthy. As thanks, the family gave Pooja a small fern of her own to care for.",
            "Which plant needed less water?", "The cactus", listOf("The fern", "Both needed the same", "The passage does not say"), "The care note said the cactus needed much less water than the fern.",
        ),
        SpeedReadingStory(
            "Farhan arrived early for a train journey and discovered that his platform had changed. He checked the departure board instead of following the crowd. On the new platform, he noticed a traveller looking confused and pointed out the updated sign. Both boarded the correct train before it left. During the trip, Farhan marked the stations in his notebook and watched the landscape change from crowded streets to open fields.",
            "How did Farhan confirm the new platform?", "Checked the departure board", listOf("Followed the largest crowd", "Called his school", "Read an old ticket envelope"), "Farhan checked the departure board when the platform changed.",
        ),
        SpeedReadingStory(
            "Asha's class organised a swap day for toys and books they no longer used. Asha brought a puzzle with all its pieces carefully counted. She chose a book about birds from another student's table. That evening, she spotted a familiar bird outside her window and found it in the book. The swap gave her a new interest while keeping useful belongings out of the rubbish.",
            "What did Asha choose at the swap?", "A book about birds", listOf("A toy train", "A box of crayons", "A wooden puzzle"), "Asha exchanged her puzzle and chose a book about birds.",
        ),
        SpeedReadingStory(
            "Neel practised a song on his flute for the school concert. He could play every note but kept rushing the ending. His music teacher asked him to tap the beat before playing that section. After several slow practice sessions, the ending became steady. On concert day, Neel took a calm breath, remembered the beat, and finished with the rest of the group at exactly the right moment.",
            "What helped Neel stop rushing the ending?", "Tapping the beat", listOf("Playing a louder flute", "Skipping the final notes", "Changing his seat"), "His teacher asked him to tap the beat and practise the ending slowly.",
        ),
        SpeedReadingStory(
            "Ritu volunteered at an animal shelter and helped prepare clean blankets for the dogs. One shy dog stayed at the back of its enclosure whenever visitors arrived. Ritu sat quietly nearby and read aloud without reaching toward it. Over several visits, the dog began coming closer. The shelter worker explained that Ritu's patient routine gave the animal time to feel comfortable around her.",
            "Why did Ritu sit quietly and read near the dog?", "To help it feel comfortable", listOf("To teach it spelling", "To keep visitors away", "To prepare it for a race"), "Her quiet, patient routine allowed the shy dog to become comfortable around her.",
        ),
        SpeedReadingStory(
            "Omar's neighbourhood lost electricity just before dinner. His family placed a battery lantern on the table and made sandwiches instead of cooking. After eating, they played a word game while waiting for the lights to return. Omar's grandfather told a story about travelling by boat as a child. When the power returned, everyone decided to finish the story before turning the television back on.",
            "What did the family do when electricity returned?", "Finished the grandfather's story", listOf("Immediately watched television", "Went out to buy dinner", "Started a boat journey"), "They chose to finish the story before turning the television on.",
        ),
        SpeedReadingStory(
            "Kavya entered a kite festival with a kite she had decorated with orange circles. At first, it spun whenever the wind strengthened. A nearby kite maker suggested adding a longer tail for balance. Kavya attached strips of spare cloth, and the kite flew steadily above the field. She spent the afternoon helping younger children decorate their tails and remembering how useful one small adjustment had been.",
            "What helped Kavya's kite fly steadily?", "A longer tail", listOf("A shorter string", "Removing its circles", "A heavier handle"), "The kite maker recommended a longer tail to improve balance.",
        ),
        SpeedReadingStory(
            "Aditya visited a museum exhibit about life before electric refrigerators. He saw clay pots, woven baskets, and photographs of shaded storage rooms. A guide explained how families kept certain foods cool using water and moving air. Back home, Aditya asked his grandmother about her childhood kitchen. Her memories helped him connect the museum objects with real meals and daily routines rather than treating them as unfamiliar decorations.",
            "Who helped Aditya connect the exhibit with everyday life?", "His grandmother", listOf("His sports coach", "A bus conductor", "His younger cousin"), "His grandmother's memories of her childhood kitchen connected the objects to daily routines.",
        ),
        SpeedReadingStory(
            "Priya and her classmates measured shadows in the playground for a science project. They marked the tip of a pole's shadow in the morning, at noon, and later in the afternoon. The marks appeared in different places, and the noon shadow was shortest. They drew a diagram of their observations. Priya made sure to record the times, because the measurements would be less useful without that information.",
            "When was the pole's shadow shortest?", "At noon", listOf("Early morning", "Late afternoon", "It stayed the same"), "The class observed that the noon shadow was the shortest.",
        ),
        SpeedReadingStory(
            "Harish planned to bake bread for a family gathering. He mixed the dough carefully, then left it in a cold corner of the kitchen. After an hour, it had barely risen. His sister suggested moving the covered bowl to a warmer spot. The dough slowly expanded, and they baked it later than planned. Guests enjoyed warm slices while Harish noted how temperature had affected the waiting time.",
            "Why was the dough moved to a warmer spot?", "It had barely risen", listOf("It was already burning", "The bowl was leaking", "The guests wanted cold bread"), "The dough barely rose in the cold corner, so his sister suggested a warmer location.",
        ),
        SpeedReadingStory(
            "Diya started a nature journal during a holiday at her uncle's farm. Each day, she drew one insect and described where she had seen it. She first assumed all the butterflies visited the same flowers. After watching carefully, she noticed different butterflies choosing different patches. Her uncle encouraged her to keep recording details, and Diya added a small map showing the flower beds beside each drawing.",
            "What did Diya discover by watching the butterflies?", "They chose different flower patches", listOf("They avoided every flower", "They all visited one tree", "They appeared only at night"), "Careful observation showed different butterflies choosing different flower patches.",
        ),
        SpeedReadingStory(
            "Yusuf helped organise a school treasure hunt with clues hidden around the courtyard. One clue directed players to a bench, but there were three benches nearby. During a trial run, his friend searched the wrong one. Yusuf rewrote the clue to mention the bench beside the mango tree. On the actual day, teams found the next envelope easily, and the hunt continued without the earlier confusion.",
            "How did Yusuf make the clue clearer?", "Mentioned the mango tree", listOf("Removed every bench", "Used a smaller envelope", "Changed the prize"), "He specified the bench beside the mango tree to distinguish it from the others.",
        ),
        SpeedReadingStory(
            "Maya wanted to learn how her town's newspaper was made. During a guided visit, she watched an editor check names and dates before approving an article. In the printing room, large sheets moved through noisy machines. Maya was surprised that checking facts took longer than she expected. For her next classroom report, she asked two people to confirm details before writing the final version for her classmates.",
            "What did Maya do differently in her next report?", "Asked two people to confirm details", listOf("Removed all names and dates", "Printed it without checking", "Wrote only about machines"), "Seeing the editor check facts prompted her to confirm details with two people.",
        ),
        SpeedReadingStory(
            "Sameer found an old wooden bird feeder in the shed. He repaired its loose roof and hung it where he could watch from the kitchen window. For several days, no birds came. His mother suggested moving it farther from the busy gate. Once it hung near a quiet shrub, sparrows began visiting. Sameer kept the feeder clean and recorded the visitors without trying to touch them.",
            "What change encouraged birds to visit the feeder?", "Moving it near a quiet shrub", listOf("Hanging it on the busy gate", "Removing the wooden roof", "Painting the kitchen window"), "Sparrows began visiting after the feeder moved away from the busy gate to a quiet shrub.",
        ),
        SpeedReadingStory(
            "Elina trained for a charity walk with her father every evening. During their first week, she tried to walk too quickly and tired before reaching the park. Her father suggested a steady pace and short water breaks. By the end of the month, she could complete the whole route comfortably. On the charity walk, she encouraged a nervous friend by walking alongside her until they crossed the finish line together.",
            "How did Elina help her friend during the charity walk?", "Walked alongside her", listOf("Carried her to the finish", "Asked her to leave early", "Ran ahead without her"), "Elina encouraged her nervous friend by staying beside her until they finished together.",
        ),
        SpeedReadingStory(
            "Manav built a model boat from scrap wood and tested it in a shallow tub. The boat tipped whenever he placed a small flag on one side. He moved the flag to the centre and added equal weights along both edges. The boat then floated upright. At the school exhibition, he showed visitors both arrangements so they could see how changing the balance affected the model's stability.",
            "What helped Manav's model boat float upright?", "Balancing the flag and weights", listOf("Emptying the tub", "Making the flag taller", "Adding weight to one side only"), "He centred the flag and placed equal weights along both edges to balance the boat.",
        ),
    )
}
