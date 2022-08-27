# BeacOnOffice
A location tracking project

This Android application project has been implemented through my Student Internship in Dialog Semiconductor (Renesas).
This app reads advertising packets from an AltBeacon device, that belongs to "Dialog SmartBond™ DA1469x" family products. 
You can read more about this technology at the link below:
https://www.renesas.com/eu/en/blogs/dialog-smartbond-da1469x-introducing-our-most-advanced-bluetooth-low-energy-product-family

These advertising packets contain three sets of data, each of which include:
1) the name of an auxiliary AltBeacon
2) the rssi signal
3) the distance between the auxiliary AltBeacon and the initiator device.

After reading and decoding these data packets, BeacOnOffice applies a triangulation algorithm designed by my teammate George Giachnakis,
that uses those three distances, which theoritically derive from the three auxiliary AltBeacons nearest to the initiator.
In this way the application calculates the current position of the initiator device inside a defined area that was part of
the company's office.
