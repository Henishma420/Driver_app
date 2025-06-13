# Driver_app
A driver-side app that receives real-time SOS requests from users in emergency via Firebase. Drivers can view, accept/reject requests, see the live distance to the SOS sender, and get the shortest path marked on an open-source map using OSMDroid + OSRM (no Google Maps API key needed!). The accepted request status auto-updates in Firebase.

Driver SOS App

Overview:

The Driver SOS App serves as the responder's interface for the SOS Emergency Sender App, enabling drivers, volunteers, or rescue personnel to receive, manage, and act on real-time emergency requests efficiently.

Core Features:

1. Real-time SOS Request Management:
The app connects to Firebase Realtime Database to fetch live emergency requests triggered by users of the SOS App.
In the “Request Menu”, the driver can browse a list of active requests containing:
Latitude & Longitude of the SOS sender.
Other request metadata like timestamp and current status (e.g., "pending", "accepted").

2. Request Acceptance Flow:
Each request can be Accepted or Rejected:
Accepted: Firebase updates the request’s status from "pending" to "accepted".
Rejected: No status change; the driver skips the request.
Once a request is accepted:
The app calculates the real-time distance between the driver’s current location and the SOS sender using their geographic coordinates.
This helps the driver assess the feasibility of reaching the person in need.

3. Shortest Route Visualization:
The shortest route from the driver’s location to the SOS sender is visually marked:
Utilizes OSRM (Open Source Routing Machine)—an open-source routing engine (no API key required).
The route is drawn using the Polyline function, highlighted in red to indicate the optimal path.
Map rendering is powered by OSMDroid, an open-source alternative to Google Maps API, offering MapView capabilities without incurring any API costs.

4. Driver Profile Management:
A dedicated Profile Section allows drivers to enter and update:
Name
Age
Phone Number
Vehicle Number
These details are stored and synced with Firebase for backend accessibility and coordination.

Technology Stack:
Firebase Realtime Database – Data sync & request handling
OSMDroid – Open-source MapView
OSRM (Open Source Routing Machine) – Route calculation (no API key needed)
Polyline Functionality – Route visualization
Android (Java)
