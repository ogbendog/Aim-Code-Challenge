Outline of service under test
The service under test maintains Stock Keeping Units with price and descriptions.
For example, the SKU "berliner" might have a price of $2.99 and a description of
"Jelly Donut".  Each Stock Keeping Unit has a unique identifier, SKU.
Items can be added, updated, and deleted.  
You can get a single SKU, for example to do a price check, or all the SKUs.

intentions of testing.
To verify all actions, Create, Read, Update, and Delete.  The tests will 
do the following:
read all the existing SKUs
create a Test SKUs then delete it (this covers two use cases)
Create a Test SKU and modify it
THen the error checking.  Tests will verify that you can't create an SKU with
any value blank or null.
The last test verifies you can't create an SKU with a negative price.

I considered adding checks to verify that the number of SKUs went up or down as they 
were added or removed, but that assumed that the API was not in use during testing.

USEAGE:
You need to have Java installed.  The tests were written to be run on a Mac10/Ubuntu 
system
the shell script provided will build the tests and run them

sh runTests.sh
 



