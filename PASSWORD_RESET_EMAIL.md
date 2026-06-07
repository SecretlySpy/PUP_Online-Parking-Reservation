# Password Reset Email Setup

The customer login screen has a `Reset Password` action. It asks for a username
or registered email address, creates a one-time password reset token, stores only
the token hash in MySQL, and emails a reset link to the registered email address.
The link expires after 30 minutes and can be used only once.

Configure SMTP before using the reset action. You can use environment variables:

```text
PARKING_SMTP_HOST=smtp.gmail.com
PARKING_SMTP_PORT=587
PARKING_SMTP_USERNAME=your-email@gmail.com
PARKING_SMTP_PASSWORD=your-app-password
PARKING_SMTP_FROM=your-email@gmail.com
PARKING_SMTP_STARTTLS=true
PARKING_SMTP_SSL=false
PARKING_PASSWORD_RESET_BASE_URL=https://your-app.example/reset-password
```

Or Java VM properties:

```text
-Dparking.smtp.host=smtp.gmail.com
-Dparking.smtp.port=587
-Dparking.smtp.username=your-email@gmail.com
-Dparking.smtp.password=your-app-password
-Dparking.smtp.from=your-email@gmail.com
-Dparking.reset.baseUrl=https://your-app.example/reset-password
```

For Gmail, use an app password rather than your normal account password.

Because this is a Swing desktop app, users can complete the reset by choosing
`Reset Password` then `Use Reset Link` and pasting the full emailed link or the
token value into the dialog.
