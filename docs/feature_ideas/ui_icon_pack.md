# UI icon pack

The UI icons were redrawn from scratch so the app no longer depends on icons of unclear licensing
(several came from the Noun Project). Prototype and review page:
[`prototypes/ui-icon-pack`](../prototypes/ui-icon-pack/ui-icon-pack.html).

## Decisions

- **Style:** filled, with rounded corners and ends. Not outline. Icons that are only a stroke
  (check, arrows, chevrons, play) are a thick rounded line; everything else is one solid shape with
  cut-outs for detail.
- **Grid:** 24x24, 2-3 px margin.
- **Colour:** no fixed colour. Vector Drawables use `android:tint="?attr/colorControlNormal"` with a
  white fill, so an `Icon(tint = ...)` at the call site still wins. The theme attribute follows the
  XML theme, not Compose's `MaterialTheme`, so it only matters where an icon is drawn without `tint`.
- **Names:** `ic_*`. Material-style names (`round_close_24`, `baseline_refresh_24`) and generic ones
  (`search`, `talk_bubble`, `think_icon`) were renamed; see the commit for the full list.
- **Scope:** UI icons only (43). Genre icons, decoration (sakura, cowboy, brush strokes) and brand
  icons (spark, cosmos, feather, leaf, emotional) are unchanged.

## Genre icons: tried, not adopted

Nine new genre icons were drawn from scratch (dragon, hat and sheriff star, ghost, caped figure,
handcuffs, shuriken, cyber eye, ringed planet, spiked stud). They were judged not good enough to
replace the current ones, which carry each genre's identity. The review page keeps them next to
the current icons for reference. Fantasy (dragon) was the weakest.

## Regenerating

`prototypes/ui-icon-pack/gen_ui_icons.py` is the single source for the 43 icons. It needs
`shapely` and writes SVG and Vector Drawable XML, which are the files in `app/src/main/res/drawable`.

## Open items

- Not verified on a device: icon size where a call site sets no size (old icons often had odd
  intrinsic sizes such as 110x135dp, the new ones are 24dp), and the theme-colour tint outside
  `Icon(tint = ...)`.
- `ic_share` and `ic_violation` changed meaning or look the most; check them in context.
- The icon licensing audit is not finished: genre and decoration icons, and the `noun_*` drawables,
  still need a decision.
