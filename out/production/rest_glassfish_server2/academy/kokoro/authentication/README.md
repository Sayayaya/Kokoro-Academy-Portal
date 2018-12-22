#Authentication

## Basics
The Portal needs a way to identify which *Person* is using the service.

In order to do this, we issue unique *Tokens* that can be used to represent a *Person*

## No Log-in UX
The Portal system is unlike typical authentication mechanisms in that users do not enter a username or password to
identify themselves. Instead, they use *Magic Links* 

## Magic Links
An in-world *LSL Script* is used to verify that the user is logged in and present at the sim.

If the user is logged in, the *LSL Script* will communicate with the Portal and authorize the creation of a *Magic Link*

A *Magic Link* can point to any page or resource on the portal. 

The link however, contains an additional query parameter: *Magic*

*Magic* is a one-use nonce, that is consumed by the act of making a request with query parameter present.






