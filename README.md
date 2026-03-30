# ADC-PEI 25/26 — individual Project 68130



#### Keynote:
tokens although they do have a role,
they always get the user role from the user
previously changing a user's role would not change
its permissions for an already lagged in session,
this way it always checks the user role

method 1
store role in token and use that for permissions

method 2
use the role stored in the User, heavier but
does restrict a signed-in user's actions

for this project I went with second approach.