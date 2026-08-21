# Configuration

[Back to README](../README.md)

Well Seasoned reads global settings from `well_seasoned.json5` in the loader
config directory. It uses the typed configuration system from Syrup Library.
The file is created automatically with the defaults on first load. A missing
file or field uses the default. Out-of-range numbers are clamped.

```json
{
  "effect_duration_stacking": {
    "mode": "logarithmic",
    "strength": 1.0
  }
}
```

`mode` selects how repeated equal-level effects stack:

| Mode | Behavior |
| --- | --- |
| `logarithmic` | Default. Reduced additions follow a logarithmic curve. |
| `linear_half` | Legacy behavior: always add half of the incoming duration, with a minimum addition of 20 ticks. |

`strength` controls how aggressive logarithmic diminishing returns are. The
default is `1.0`. A value of `0.0` disables diminishing returns. The valid range
is `0.0` through `100.0`.

## Logarithmic duration stacking

When a player already has the same effect at the same level, Well Seasoned adds
a reduced portion of the incoming duration:

```text
addedDuration = incomingDuration / (1 + strength * log2(1 + currentDuration / incomingDuration))
```

The reduction depends on the ratio between the active duration and the incoming
duration. Thus, it behaves the same way for short and long food effects.

At strength `1.0`, stacking onto an equal remaining duration adds exactly half
of the incoming duration. The final duration still cannot exceed
`maximum_duration`.

`strength` examples when the current duration equals the incoming duration:

| Strength | Incoming duration added |
| ---: | ---: |
| `0.0` | 100% |
| `0.5` | ~66.7% |
| `1.0` | 50% |
| `1.5` | 40% |
| `2.0` | ~33.3% |
