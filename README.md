# Iluminus

[iLuminus App](https://github.com/rodriguessdeyson/iluminus-app) lets colors flow through you. Use your feelings to choose hues that reflect your emotions, and allow yourself to experience the calming and relaxing effects after your day.

## Iluminus Firmware

**Iluminus Firmware** is an **Arduino-based non-blocking LED effects engine** for controlling WS2812 / NeoPixel strips (or compatible LEDs).
It allows you to trigger multiple lighting effects (Cylon bounce, rainbow, theater chase, sparkle, fade, etc.) via **serial commands** or function calls, all without blocking delays, making it ideal for real-time embedded applications.

## Features

- Supports **Adafruit NeoPixel** (and can be extended to FastLED).
- Non-blocking animations using `millis()`.
- Multiple effects: Static on/off, Sleep modes, Fade in/out, Rainbow cycle, Theater chase, Cylon bounce, Sparkle & Snow sparkle, Parameterized (speed, size, brightness) and RGB color input supported.

## Hardware

The project was developed and tested using the following setup:
- Arduino Uno / Nano / Micro / Mega (or compatible);
- WS2812 / NeoPixel LED strip (tested with **18 LEDs**, configurable);
- Power supply 9V ~ 12V- 2A. Adjust it based on the amount of leds and Watt's law.
- One digital pin for data output (`PIN 5` by default).

## Usage

Control the LED strip via Serial Monitor. Default baud rate: 115600.

The command format must follow: COMMAND,R,G,B.

Where:

| Command | Effect        | Params Example                      |
| ------- | ------------- | ------------------------------------|
| `L`     | LED On        | `L,255,0,0` (red)                   |
| `D`     | LED Off       | `D`                                 |
| `S`     | Sleep Mode    | `S,255,0,0` (red effect)            |
| `F`     | Fade Color    | `F,0,210,200`                       |
| `C`     | Cylon Bounce  | `C,255,0,0,4,20`                    |
| `O`     | Fade In/Out   | `O,0,0,255` (blue)                  |
| `P`     | Sparkle       | `P,255,255,255` (white sparkle)     |
| `N`     | Snow Sparkle  | `N,0,0,255,100` (blue bg, 100ms)    |
| `R`     | Rainbow Cycle | `R,0,0,0` (30ms step)               |
| `T`     | Theater Chase | `T,0,0,0` (50ms step)               |


## License
MIT License © 2025 — Free to use, modify, and share.