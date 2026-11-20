# Frosted Foliage

A client-side Fabric mod for Minecraft Java **26.3**. When it snows, plants that are open to the sky turn white a
little at a time, like the snowy leaves in Bedrock Edition. When the snow stops they slowly turn back.

It only changes how things look, so it works in singleplayer and on any server, with no server install needed.

## What turns white

Everything that grows, and each kind can be switched off on its own (Settings > the plant group buttons):

- **Leaves**: oak, spruce, birch, jungle, acacia, dark oak, mangrove, cherry, pale oak, azalea, flowering azalea,
  the three poplar leaves, and leaves added by other mods
- **Grass and ground cover**: grass blocks, short and tall grass, ferns, bushes, dry grass, dead bushes, red shrubs,
  leaf litter, pink petals, wildflowers, vines, moss, pale moss
- **Flowers**: every small and tall flower, torchflower, pitcher plant, eyeblossoms
- **Crops**: wheat, carrots, potatoes, beetroots, melon and pumpkin stems, torchflower and pitcher crops
- **Saplings**: all of them, propagules and bamboo shoots too
- **Other plants**: sugar cane, bamboo, sweet berry bushes, mushrooms, azalea bushes, dripleaves

A plant only whitens when snow can actually reach it:

- it stands in a place where rain falls as snow (a cold biome, or high up a mountain), and
- it is open to the sky. Plants under a tree, a roof or a cave ceiling stay as they are. A tree turns white all over
  its outside (the top and every side that has open sky next to it); leaves buried deep inside the canopy stay green.

## Settings

Open them from Mod Menu, or bind **Open Frosted Foliage settings** under Controls.
There is also an unbound key for **Toggle Frosted Foliage**.

| Setting | What it does |
| --- | --- |
| Frosted Foliage | Master switch |
| Time to Whiten | Seconds of snowfall until plants are completely white (5 to 600, default 90) |
| Time to Melt | Seconds after the snow stops until the white is gone (0 to 1800, default 180). 0 means never |
| Variation | How unevenly plants whiten. 0 turns them all at once; higher lets some go white sooner |
| Whiten Sides | On (default): anything with open sky beside it turns white, so the whole outside of a tree is white. Off: only what is open from above, which is just the top layer of a tree |
| Cover Allowed | How many blocks of cover may sit above a plant and still count as open to the sky (0 to 6, default 1). 0 is strict; higher whitens leaves deeper inside a tree |
| Smoothness | How many stages the change is split into. More is smoother, but chunks are rebuilt more often |
| Plant groups | One on/off switch per plant, with Turn All On / Turn All Off |

Settings are stored in `config/frostedfoliage.json`.

## Preview without waiting for snow

```
/frostedfoliage preview 60
/frostedfoliage preview off
```

The first command shows every plant at 60 percent frost until you run the second (the numbers go from 0 to 100).
Plants still have to be in a snowy place and open to the sky.

## Notes

- Fully white needs the whole `Time to Whiten` of snowfall. The effect is baked into the chunk meshes, so while it is
  building up, chunks near you are rebuilt a few at a time, nearest first.
- The white textures are made when resources load, from whatever textures your resource packs use, so texture packs
  with their own plants work.
- Changing which plants are on, Variation, Whiten Sides, Cover Allowed or Smoothness rebuilds the chunks around you once when you
  close the settings.
- Needs Fabric Loader 0.19+, Fabric API and Java 25. Mod Menu is optional.

## Building

```
./gradlew build
```

The jar is in `build/libs/`. Builds need JDK 25.
