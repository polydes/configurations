+++
title = "Engine Extension Integration"
+++

## Engine Extension Integration

Let's say we have an engine extension, `coolsiteApi`, that provides the API for integration on a publisher's website, `coolsite`.

This is what the content of an engine extension may typically look like:

```
engine-extensions/
└─coolsiteApi/
  ├─blocks.xml
  ├─include.xml
  ├─info.txt
  └─src/
```

#### Create the haxedef

Create a file called `defines.xml` at the top level of the engine extension.

`defines.xml`:
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<defines>
  <define name="coolsite" description="Enables the Coolsite API." />
</defines>
```

| property    | value |
| ----------- | ----- |
| name        | The unique id of this define. Can be set to anything, but it should be unique among all named definitions, including those contributed by other engine extensions. |
| description | A short description that says what effect enabling this haxedef will have on a Stencyl project. |

Now, we can use the new definition.

#### Modify `include.xml`

First, let's modify the extension's `include.xml` file to have no effect if the new definition isn't enabled.

The existing `include.xml` file:
```xml
<?xml version="1.0" encoding="utf-8"?>
<project>
  <source path="src" />
  <dependency name="https://api.coolsite.com/coolsite-api.js" />
</project>
```

`include.xml` here adds some a dependency to `coolsite` to our game. This is great if we're publishing there, but if not, we'd like some way to disable all that without removing the entire engine extension. So, we can guard everything with `<section if="coolsite">` to prevent any of this from being included unless the `coolsite` define is enabled.

`include.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<project>
  <section if="coolsite">
    <source path="src" />
    <dependency name="https://api.coolsite.com/coolsite-api.js" />
  </section>
</project>
```

> 📝 **Note:** this technique only works if the source code for the extension is not found at the top level of the engine extension. For example, if your engine extension looks like this, the source code will be automatically included regardless of what's specified in `include.xml`:
> 
> ```
> engine-extensions/
> └─coolsiteApi/
>   ├─blocks.xml
>   ├─include.xml
>   ├─info.txt
>   └─MySourceCode.hx <- (can't exclude this source)
> ```

#### Add a new block

If the engine extension has blocks that rely on the `coolsite` integration, we want to avoid using those if the game isn't configured for distribution on coolsite. Add a block like this to the extension's `blocks.xml` file.

```xml
  <block
    tag="def-coolsite"
    spec="coolsite is enabled"
    code="coolsite"
    type="normal"
    color="charcoal"
    returns="boolean">
    <fields />
  </block>
```

| property    | value |
| ----------- | ----- |
| tag         | Arbitrary, but it should be unique among all blocks across all extensions. |
| spec        | Since this is the human readable label on the block, this can be anything. |
| code        | Should be exactly equal to the id of the define. |
| type        | "normal" |
| returns     | "boolean" |

In design mode, you can use the `# if <coolsite is enabled>` block to include coolsite-related code only if it's defined.

```
╔═ # if <[coolsite is enabled]> ═══
║ ═ show coolsite ads ═
╚══════════════════════════════════
```