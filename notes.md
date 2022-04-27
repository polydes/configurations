Haxedefs can be registered in the following places:
- TODO: globally (`com.polydes.configurations.eprefs`)
- TODO: per-game (`extension-data/com.polydes.configurations/user-defines.xml`)
- in engine extensions (`defines.xml`)


- games should be able to be compiled to different places for different configurations
- The difference between debug/release could be configurations
  - Debug
    - host architecture only [native]
    - debug
  - Test
    - host architecture only [native]
  - Publish
    - all architectures [native]
- you should be able to publish multiple configurations at once
  - then perhaps configurations should only choose one architecture?
  - for windows, individual architectures
  - but for an aab or mac app, multi-arch is the way to go.
