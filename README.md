# Configurations Extension

This extension provides the capability to use haxedefs to make configurations for your game. Using haxedefs, you can add conditional compilation to behaviors, and have conditional segments in your advanced OpenFL settings.

Haxedefs can be registered in the following places:
- TODO: globally (`com.polydes.configurations.eprefs`)
- TODO: per-game (`extension-data/com.polydes.configurations/user-defines.xml`)
- in engine extensions (`defines.xml`)

Haxedefs may then be grouped together into a single configuration. Set your desired configuration, and then when you test/debug/publish your game, it will use all the haxedefs in the given configuration to determine how the game is built.

Example:

Let's say we have an engine extension, `coolsiteApi`, that provides the API for integration on a publisher's website, `coolsite`.

`defines.xml`:
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<defines>
	
	<define name="coolsite" description="Enables the Coolsite API." />
	
</defines>
```

`include.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<project>
	<section if="html5 coolsite">
		<source path="src" />
		
		<section if="publishing">
			<dependency name="https://api.coolsite.com/coolsite-api-v1.js" />
		</section>
		
		<section unless="publishing">
			<!-- Only include sdk.html for local testing. -->
			<template path="templates" />
			<!-- Use a local copy since we can't access this through xhr from localhost. -->
			<dependency path="dependencies/coolsite-api-v1.js" embed="true" />
		</section>
		
	</section>
</project>
```

`include.xml` adds some dependencies to `coolsite` to our game. This is great if we're publishing there, but if not, we'd like some way to disable all that without removing the entire engine extension. So, we guard everything with `<section if="html5 coolsite">` to prevent any of this from being included unless we're targetting HTML5 **and** the `coolsite` define is enabled.

In design mode, you can use the `# if <coolsite>` block to include coolsite-related code only if it's defined.

```
╔═ # if <[coolsite]> ═══
║ ═ comment [we're running the coolsite configuration, so we have access to the coolsite blocks] ═
║ ═ show coolsite ads ═
╚═══════════════════════
```