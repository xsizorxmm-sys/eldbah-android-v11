# تطبيق مؤسسة الدبّاح – Android

مشروع Android يفتح متجر مؤسسة الدبّاح داخل WebView.

## بناء نسخة الاختبار (Debug)
من تبويب **Actions** شغّل **Build Eldbah Android APK**. بعد نجاح البناء نزّل artifact باسم `eldbah-v11-debug-apk`.

## بناء نسخة Release موقّعة
يدعم Workflow إنشاء APK و Android App Bundle (AAB) موقّعين تلقائيًا عند إضافة أسرار التوقيع إلى إعدادات المستودع.

من GitHub افتح:
**Settings → Secrets and variables → Actions → New repository secret**

أضف الأسرار الأربعة التالية:

- `ELDBAH_KEYSTORE_BASE64`: محتوى ملف keystore بعد تحويله إلى Base64.
- `ELDBAH_STORE_PASSWORD`: كلمة مرور ملف keystore.
- `ELDBAH_KEY_ALIAS`: اسم المفتاح داخل keystore.
- `ELDBAH_KEY_PASSWORD`: كلمة مرور المفتاح.

**مهم:** احتفظ بنسخة احتياطية آمنة من ملف keystore وكلمات المرور. لا ترفع ملف keystore أو كلمات المرور إلى GitHub أو إلى ملفات المشروع. فقدان المفتاح يعني عدم القدرة على تحديث التطبيق المثبت بنفس هوية التوقيع.

بعد إضافة الأسرار، شغّل Workflow مرة أخرى من **Actions → Build Eldbah Android APK → Run workflow**. سيُنشئ artifact إضافيًا باسم `eldbah-v11-release` يحتوي على APK وAAB موقّعين. إذا لم تُضف الأسرار، سيستمر بناء نسخة Debug فقط دون فشل.
