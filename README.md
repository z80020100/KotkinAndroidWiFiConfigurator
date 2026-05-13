# Wi-Fi Configurator

A Kotlin Android dev tool for registering Wi-Fi network suggestions via adb broadcast. Useful for lab and QA setups where a host script needs to push Wi-Fi configuration to test devices without manual entry.

## Requirements

- Android 10 (API 29) or later

## Build and install

```bash
./gradlew :app:installDebug
```

## Usage

```bash
adb shell am broadcast \
  -a com.example.wi_ficonfigurator.action.SUGGEST_WIFI \
  --es ssid "SSID" \
  --es password "PASSWORD" \
  --ez hidden false
```

### Extras

| Extra | Type | Required | Default | Description |
|---|---|---|---|---|
| `ssid` | string | yes | — | Target SSID |
| `password` | string | no | (empty) | Passphrase for WPA2-Personal or WPA3-Personal; omit for Open or OWE |
| `hidden` | bool | no | `false` | Whether the SSID is hidden (enables active probe) |

Inspect the result:

```bash
adb logcat -s WifiSuggestionReceiver
```

A successful run prints `addNetworkSuggestions ssid='...' hidden=... status=SUCCESS`. After the suggestion is added the system shows an approval prompt; once approved the device associates automatically when the network is in range.

## Behavior

Two `WifiNetworkSuggestion` entries are registered per call to cover security transition modes without the caller knowing the AP's exact configuration:

| `password` extra | Suggestions registered | AP types covered |
|---|---|---|
| empty or absent | `[Open, OWE]` | Open (unencrypted), OWE (encrypted without a password), OWE Transition Mode (AP advertises both Open and OWE) |
| non-empty | `[WPA2-PSK, WPA3-SAE]` | WPA2-Personal (PSK), WPA3-Personal (SAE), WPA3-Personal Transition Mode (AP advertises both WPA2 and WPA3) |

Different `WifiNetworkSuggestion` security types are stored as distinct entries and the framework only matches the variant that fits the actual AP at runtime; the unused variant is inert.

## Limitations

- **WEP** is unsupported — Android's `WifiNetworkSuggestion.Builder` provides no WEP setter.
- **WPA2-Enterprise** and **WPA3-Enterprise** networks and **Passpoint** (Hotspot 2.0) are out of scope; their credential schemas do not fit intent extras.
- The receiver is exported so adb broadcasts can reach it; any installed app can also broadcast `SUGGEST_WIFI`. Suggested networks still require system approval before any auto-connect; the worst case for a rogue broadcast is suggestion clutter rather than auto-connect.
