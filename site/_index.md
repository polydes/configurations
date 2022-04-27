+++
[extra]
	blurb = "Configure a single project to export with different configurations. For example, different SDKs for different HTML5 portals."
+++

This extension provides the capability to use haxedefs to make configurations for your game. Using haxedefs, you can add conditional compilation to behaviors, and have conditional segments in your advanced OpenFL settings.

Haxedefs can be registered in the following places:
- [x] in engine extensions (`{engine-extension}/defines.xml`)

Possible places where Haxedefs can be registered in the future:
- [ ] globally (`{sw-prefs}/com.polydes.configurations.eprefs`)
- [ ] per-game (`{game}/extension-data/com.polydes.configurations/user-defines.xml`)

Haxedefs may then be grouped together into a single configuration. Set your desired configuration, and then when you test/debug/publish your game, it will use all the haxedefs in the given configuration to determine how the game is built.

See [Engine Extension Integration](guides/engine-extension-integration) for an example.