<div align="center">
    <img src="fabric/src/main/resources/assets/systemsapi/header.png" alt="SystemsAPI Icon">
</div>

# SystemsAPI

> [!WARNING]
> This library is a work in progress! Expect constant changes at this stage.

SystemsAPI is a fabric library that abstracts Hypixel Housing's house systems for other mods to use. For an example implementation, see [HTSL Reborn](https://github.com/sinender/HTSLReborn).

## Features:
- [x] Action importing
  - [x] Error correction (catches when things don't go how they should, and tries again automatically)
  - [ ] Optimized importing (optimally keeps/changes existing actions to make imports quick!)
- [x] Action exporting
- [ ] System abstraction (methods for programmatically interacting with any/all Housing systems.)
    - [x] Functions
    - [ ] Events
    - [ ] Commands
    - [ ] Custom Menus
    - [ ] Regions
    - [ ] Scoreboard