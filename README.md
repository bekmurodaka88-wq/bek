# Android Pedometr ilovasi

Bu loyiha Android telefonlar uchun oddiy pedometr (qadam sanagich) ilovasi.

## Xususiyatlar
- `TYPE_STEP_COUNTER` sensori bo'lsa, aniq qadam hisoblash.
- Sensor bo'lmasa, akselerometr orqali fallback hisoblash.
- Jetpack Compose asosidagi sodda UI.
- Qadamlarni nolga qaytarish (reset) tugmasi.

## Ishga tushirish
1. Android Studio (Hedgehog yoki yangiroq) bilan oching.
2. Gradle sync tugaguncha kuting.
3. Qurilma yoki emulatorga `Run` qiling.

## Eslatma
Akselerometr fallback usuli taxminiy hisoblaydi; `STEP_COUNTER` mavjud qurilmalarda natija aniqroq bo'ladi.
