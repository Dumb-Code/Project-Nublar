# Project Nublar Fence Demo (2019) - behaviour notes

Source: https://www.youtube.com/watch?v=sm1PlzE5rlQ (Dumb::Code, 2019-02-17, 1:07). No spoken narration or captions file exists; the only text is on-screen captions. Programmer write-up (not read here): github.com/Dumb-Code/ProjectNublar/wiki/Fences.
Method: frames at 1 fps, plus 3.5 fps for 19-28 s and 2 fps for 0-5 s, viewed as contact sheets. Timestamps are plus or minus 1 s. Audio was not analysed, so sounds are unknown.
Video description: fences are "omni-directional and very flexible".

## Segments (on-screen caption, in video order)

### 0:00-0:18 "Powering - Actual powering method is still WIP" (night)
- 0:00-0:01 black fade-in.
- Player stands at a post that has a grate-style base plate at its foot. A dark box block with a grille top and a cyan panel sits in the lower right of the screen next to the post. It is probably the power source. The caption says the real powering method is work in progress. UNCERTAIN what the block is.
- Hotbar: one item, a grey post-shaped item.
- Standard post: thin grey pole, black insulators on the side, 5-6 wires per span, top bends over in a hook shape with an orange lamp on the tip.
- Power look: lamp is orange when powered. In the first dark frames the far lamp shows red or nothing. UNCERTAIN because of the dark.
- 0:09-0:18: camera goes to an overhead view of a large closed oval ring of fence on rolling terrain. Every post gives a soft yellow glow on the ground (posts are light sources). The lit arc grows around the ring over several seconds (right side first, then clockwise to the bottom, left, top), so power seems to travel post to post. Whole ring lit by about 0:17.
- The ring follows hills and curves, so posts and spans sit at many angles.

### 0:18-0:23 "Collision" (night)
- Third-person view of the player (default skin, 10 hearts, 10 hunger) running at the fence. Hotbar slot 9 selected, empty.
- About 0:20 the player reaches the wires: the whole player model flashes RED (hurt), small green-yellow spark particles appear around the player, and hearts drop by about one (a frame shows 9 and a half). The player is NOT stopped: they keep running through the wire plane. UNCERTAIN: it may be a hard push that is not visible; no knockback seen.
- 0:21-0:23 view from above and behind the player with a pond ahead; two posts appear to lie flat or lean strongly on both sides of the player. UNCERTAIN: could be the camera angle, or fallen/unpowered posts.

### 0:23-0:28 "Unpowering" (night)
- First-person view at the power post: base plate with the grate, power box at the lower right.
- About 0:25 a hand item with cyan, red, green, orange, white pixels appears (looks like a controller or remote). The base plate shows noisy red and cyan pixels. UNCERTAIN: probably switching the power off. No GUI.
- 0:25-0:28 wires and post lamps go dark (no more orange). The yard stays dark.

### 0:28-0:38 "Placement" (day, grass plains)
- New daytime world. Hotbar: slot 1 a grey bent post item (orange tip, held in hand), slot 2 a stack of 64 of a bucket-like item (grey cylinder, brown cap). The tooltip shows the raw key "tile.electric_fence.name" (missing lang entry). The player holds the 64 stack for most of the placement.
- Player right-clicks the grass: a post appears with a block selection outline. The post is a grey pole with black insulator rings along its side (about 6 per post).
- 0:30-0:35 posts get placed some blocks apart; wires (6 per span) join each insulator to the matching insulator on the next post. A middle post carries wires on both sides (two spans) and wires can leave in different directions.
- Wires run up and down slopes: the wire angle follows the height difference between posts (0:35-0:37 wires climb a hill).
- No GUI. UNCERTAIN: whether the bucket-like item places posts or wires.
- Post height: about 3 blocks for the standard post. UNCERTAIN.

### 0:38-0:43 "Breaking" (day)
- Player has an empty hand, 10 hearts, slot 2 selected. A bucket-like item sits in slot 4.
- Player left-clicks one wire. Only that wire disappears; the others stay. The cut wire stays as bent stubs at both ends (kinked, drooping) and some wires come down toward the ground. About 0:40-0:42 many small glyph-like pixel particles float where the wire broke (look like enchanting-table glyphs). UNCERTAIN: could be break particles.
- Player breaks more wires one at a time; the top 3-4 wires are gone and stubs remain at both posts, lower wires stay.

### 0:43-0:48 "Replacement" (day)
- Wire stubs lie on the grass in a line (broken pieces on the ground).
- Hotbar has more stacks (slots 1-5). Player selects slot 4 (bucket-like item) and uses it on the broken area. The wire segments reconnect between the posts. UNCERTAIN: exact click target (stub, post, or air).
- 0:45-0:47 a lone post stands and the item is used again to restore wires toward the next post.

### 0:48-0:53 "Item Animation" (day)
- First-person hand item: brown cap on a grey cylinder. Item moves/rotates when used (the cap turns in the hand). Aims at a far post.
- 0:50 close-up: a post with 3 insulator barrels (black cylinder plus ring) sticking out sideways.
- 0:51-0:52 an empty hand next to a post whose three insulators now carry horizontal wire rods. Probably the wire being attached. UNCERTAIN.

### 0:53-1:02 "High security fences" (day)
- Taller, straighter posts on hills: light grey pole, dark RED bands at each wire slot, a striped lamp cap (red/orange/blue) on top, no hook bend. 6-7 wires per span.
- The fence follows hill terrain, curving along ridges; spans slope with the ground. Long runs seen from the air (0:56-1:00).
- White blocks (animals, probably sheep) stand in the field. No interaction with the fence seen. UNCERTAIN.
- 1:00-1:02 wires going up at a steep angle across a gap (incline).

### 1:02-1:07 "Volts sign" (day)
- Player (empty hand, slot 6) looks up along the wires.
- Warning signs (red/white, lightning hand, "1000 VOLT") hang on the wires, lying over 2-3 wires each at an angle. One sign at 1:02, two at 1:03, three at 1:04-1:05. A small red-white item shows in the hotbar at 1:03. Video ends at about 1:07.

## Summary
- Post types: (1) standard post, hook bend top, orange lamp, black insulators, 6 wires; (2) high-security post, straight, red bands, striped lamp cap, taller, 6-7 wires.
- Power: dark machine block with grille and cyan panel next to a post, WIP. Power spreads post to post. Lamps orange when on; posts glow yellow on the ground.
- Contact: player flashes red, green sparks, loses about 1 heart; no solid block seen, no clear knockback.
- Wires break one by one with a click; ends kink and droop; replacement tool restores them.
- Fence follows terrain and any angle (ring, slopes). No GUI anywhere. Volt warning signs can be put on wires.
- Mobs: none interact.

## Unclear
- Power block identity and how power is switched off (0:24-0:28).
- Real knockback or push-back.
- Which item (bucket-like stack) does placement, wires, or repair.
- Raw lang key on item names, so real names unknown.
- Flat or leaning posts at 0:21-0:23.
- No audio checked.
