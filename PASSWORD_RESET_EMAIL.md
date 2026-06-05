# Password Reset Email Setup

The user login screen has a `Forgot Password` action. It finds a customer
account by username or registered email address, creates a temporary password,
updates the account, and emails the temporary password to the registered email
address.

Configure SMTP before using the reset action. You can use environment variables:

```text
PARKING_SMTP_HOST=smtp.gmail.com
PARKING_SMTP_PORT=587
PARKING_SMTP_USERNAME=your-email@gmail.com
PARKING_SMTP_PASSWORD=your-app-password
PARKING_SMTP_FROM=your-email@gmail.com
PARKING_SMTP_STARTTLS=true
PARKING_SMTP_SSL=false
```

Or Java VM properties:

```text
-Dparking.smtp.host=smtp.gmail.com
-Dparking.smtp.port=587
-Dparking.smtp.username=your-email@gmail.com
-Dparking.smtp.password=your-app-password
-Dparking.smtp.from=your-email@gmail.com
```

For Gmail, use an app password rather than your normal account password.
