Outline of service under test

The SUT maintains Stock Keeping Unit Identifiers with associated price and descriptions.
For example, the SKU "berliner" might have a price of $2.99 and a description of
"Jelly Donut".  
SKUs can be added, updated, and deleted.  
You can GET a single SKU, for example to do a price check, or GET all the SKUs.

Intentions of testing.
To verify all HTTP actions, Create, Read, Update, and Delete.  
The tests will do the following:
Read all the existing SKUs and verify more than 1 is returned.
Create a Test SKUs then delete it (this covers two use cases.
Create a Test SKU and modify it.

Once the basic tests are covered there are a series of error checking tests.
They will verify that you can't create an SKU with any value blank or null.
The last test verifies you can't create an SKU with a negative price.

I considered adding checks to verify that the number of SKUs went up or down as they 
were added or removed, but that assumed that the API was not in use during testing.

USEAGE:
You need to have Java installed.  The tests were written on a Mac10/Ubuntu 
system.  
the shell script provided will build the tests and run them.  Has I a windows machine
with Java, I would have also provided a .bat

To run the tests, simply pull them, and run the shell script runTests.sh as follows:

sh runTests.sh
 



