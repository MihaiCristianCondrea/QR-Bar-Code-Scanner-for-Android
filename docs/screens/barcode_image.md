# Barcode Image Screen

## Entry point contract

- **Activity:** `com.d4rk.qrcodescanner.plus.ui.screens.barcode.BarcodeImageActivity`
- **Required arguments:**
  - `EXTRA_BARCODE` (`com.d4rk.qrcodescanner.plus.extra.BARCODE`): a serialized instance of `com.d4rk.qrcodescanner.plus.model.Barcode`.
- **Intent helpers:** Use `BarcodeImageActivity.createIntent(context, args)` or `BarcodeImageActivity.start(context, barcode)` to guarantee the required extra is always supplied.

## Empty-state behavior

When launched without the required barcode extra, the screen now displays an inline error message with a confirmation button so the user can gracefully return to the previous screen.

