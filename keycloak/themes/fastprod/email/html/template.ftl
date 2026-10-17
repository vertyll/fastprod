<#-- The frame of every e-mail, drawn as the app draws its cards: always light, as the app is. Mail clients read neither CSS
     variables nor light-dark(), so the app's colors are written here as hex. -->
<#macro emailLayout>
<!DOCTYPE html>
<html lang="${locale.language}" dir="${(ltr)?then('ltr','rtl')}">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="color-scheme" content="light">
    <meta name="supported-color-schemes" content="light">
    <style>
        .text a { color: #1c2a3a; text-decoration: underline; }
    </style>
</head>
<body class="page" style="margin: 0; padding: 0; background: #ffffff;">
<table role="presentation" width="100%" cellpadding="0" cellspacing="0" class="page" style="background: #ffffff;">
    <tr>
        <td align="center" style="padding: 32px 16px;">
            <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="max-width: 560px;">
                <tr>
                    <td class="text" style="padding: 0 4px 16px; font-family: -apple-system, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; font-size: 20px; font-weight: 700; color: #1c2a3a;">${realmName}</td>
                </tr>
                <tr>
                    <td class="card text" style="background: #ffffff; border: 1px solid #d7dce3; border-radius: 8px; padding: 24px; font-family: -apple-system, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; font-size: 14px; line-height: 1.6; color: #1c2a3a;">
                        <#nested>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>
</body>
</html>
</#macro>
