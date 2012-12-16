CS158B-Project
==============
Within this software suite there are:
	3 Device library packages
		Client
		NESimulator
		RMON
	2 Supplement library packages
		Crypto
		Structure
		
Client
This package contains all necessary classes to run the
Client Interface of the network management system. This
package is dependent upon the Crypto and Structure
packages.

NESimulator
This package contains all necessary classes to run a
Network Element Simulated device on the network. This
package is dependent upon the Crypto and Structure
packages.

RMON
This package contains all necessary classes to run the
Remote Monitoring entity which interacts with Clients
and NEAgents on the network. This package is dependent
upon the Crypto and Structure packages.