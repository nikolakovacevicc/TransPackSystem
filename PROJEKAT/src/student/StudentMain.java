package student;

import rs.etf.sab.operations.CityOperations;
import rs.etf.sab.operations.CourierOperations;
import rs.etf.sab.operations.CourierRequestOperation;
import rs.etf.sab.operations.DistrictOperations;
import rs.etf.sab.operations.GeneralOperations;
import rs.etf.sab.operations.PackageOperations;
import rs.etf.sab.operations.UserOperations;
import rs.etf.sab.operations.VehicleOperations;
import rs.etf.sab.tests.TestHandler;
import rs.etf.sab.tests.TestRunner;




public class StudentMain {

    public static void main(String[] args) {
        CityOperations cityOperations = new kn210113_City(); // Change this to your implementation.
        DistrictOperations districtOperations = new kn210113_District(); // Do it for all classes.
        CourierOperations courierOperations = new kn210113_Courier(); // e.g. = new MyDistrictOperations();
        CourierRequestOperation courierRequestOperation = new kn210113_CourierRequest();
        GeneralOperations generalOperations = new kn210113_General();
        UserOperations userOperations = new kn210113_User();
        VehicleOperations vehicleOperations = new kn210113_Vehicle();
        PackageOperations packageOperations = new kn210113_Package();

        TestHandler.createInstance(
                cityOperations,
                courierOperations,
                courierRequestOperation,
                districtOperations,
                generalOperations,
                userOperations,
                vehicleOperations,
                packageOperations);

        TestRunner.runTests();
    }
}
