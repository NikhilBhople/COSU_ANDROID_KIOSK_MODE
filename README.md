# COSU_ANDROID_KIOSK_MODE
Android includes APIs to manage devices that are dedicated to a specific purpose. 
This repo introduces these APIs. If you're an enterprise mobility management (EMM) developer or solution integrator, 
then clone the repo to get started.

Where are dedicated devices used?
Dedicated devices (formerly called corporate-owned single-use, or COSU) are fully managed devices that serve a specific purpose. 
Android provides APIs that can help you create devices that cater to employee- and customer-specific needs:

Employee-facing: Inventory management, field service management, transport and logistics
Customer-facing: Kiosks, digital signage, hospitality check-in
Dedicated device features
Android includes APIs to help people using dedicated devices focus on their tasks. 
You typically call these APIs from a custom home app that you develop. 

#Your custom home app can use some, or all, of the following APIs:

* Run the system in an immersive, kiosk-like fashion where devices are locked to a whitelisted set of apps using lock task mode.
* Share a device between multiple users (such as shift workers or public-kiosk users) by managing ephemeral and secondary users.
* Avoid devices downloading the same app again for each temporary user by caching app packages.
* Suspend over-the-air (OTA) system updates over critical periods by freezing the operating system version.
* To call these APIs, apps need to be the admin of a fully managed device

# for setting app as device owner.
Run the following command in ADB

# Device Owner Command
adb shell dpm set-device-owner nikhil.bhople.kioskdemoapp/.kiosk.DeviceAdmin

You can exit kiosk mode by clicking EXIT button in app
And if you want to remove device owner and exit then click on REMOVE DEVICE OWNER AND EXIT button in app

