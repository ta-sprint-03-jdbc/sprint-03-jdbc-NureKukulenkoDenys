package dao;

import model.Child;
import org.junit.jupiter.api.*;
import utils.DBUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Child Database Operations Tests")
class ChildDBTest {

    private ChildDB db;

    @BeforeEach
    void setUp() throws SQLException, IOException {
        new DBUtil().executeFile("init.sql");
        db = new ChildDB();
    }

    @AfterEach
    void tearDown() throws Exception {
        try (Connection conn = DBUtil.getConnection();
             Statement st = conn.createStatement()) {
            st.execute("TRUNCATE TABLE child CASCADE");
        }
        db.close();
    }

    @Test
    @DisplayName("Should add a child and return it with an ID")
    void addShouldAddChildAndReturnWithId() throws SQLException {
        // Arrange
        String firstName = "John";
        String lastName = "Doe";
        LocalDate birthDate = LocalDate.of(2010, 1, 1);
        Child child = new Child(firstName, lastName, birthDate);

        // Act
        Child addedChild = db.addChild(child);
        System.out.println("[DEBUG_LOG] Added child ID: " + addedChild.id());

        // Assert
        assertNotNull(addedChild.id(), "Child ID should not be null");
        assertEquals(firstName, addedChild.firstName(), "First name should match");
        assertEquals(lastName, addedChild.lastName(), "Last name should match");
        assertEquals(birthDate, addedChild.birthDate(), "Birth date should match");
    }

    @Test
    @DisplayName("Throw IllegalArgumentException when child is null")
    void shouldThrowException_whenChildIsNull() {
        //Act and Assert
        assertThrows(IllegalArgumentException.class, () -> db.addChild(null),
                "should throw IllegalArgumentException wehn child is null");
    }

    @Test
    @DisplayName("Throw IllegalArgumentException when child first name is null")
    void shouldThrowException_whenFirstNameIsNull() {
        // Arrange
        Child child = new Child(null, "Doe", LocalDate.now());
        //Act and Assert
        assertThrows(IllegalArgumentException.class, () -> db.addChild(child),
                "should throw IllegalArgumentException when first name is null");
    }

    @Test
    @DisplayName("Throw IllegalArgumentException when child lastname is null")
    void shouldThrowException_whenLastNameIsNull() {
        // Arrange
        Child child = new Child("John", null, LocalDate.now());
        //Act and Assert
        assertThrows(IllegalArgumentException.class, () -> db.addChild(child),
                "should throw IllegalArgumentException when child lastname is null");
    }

    @Test
    @DisplayName("Should throw SQLException when connection is closed")
    void addChildShouldThrowSQLException_WhenConnectionIsClosed() throws Exception {
        // Arrange
        Connection conn = DBUtil.getConnection();
        conn.close();

        ChildDB brokenDb = new ChildDB(conn);
        Child child = new Child("John", "Doe", LocalDate.of(2010, 1, 1));

        // Act + Assert
        SQLException ex = assertThrows(
                SQLException.class,
                () -> brokenDb.addChild(child),
                "should throw SQLException when connection is closed"
        );

        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Should add a child with null birth date")
    void addShouldHandleNullBirthDate() throws SQLException {
        // Arrange
        String firstName = "James";
        String lastName = "Cook";
        LocalDate birthDate = null;
        Child child = new Child(firstName, lastName, birthDate);
        // Act
        Child addedChild = db.addChild(child);
        System.out.println("[DEBUG_LOG] Added child with Null birthDate ID: " + addedChild.id());
        // Assert
        assertNotNull(addedChild.id(), "Child ID should not be null");
        assertEquals(firstName, addedChild.firstName(), "First name should match");
        assertEquals(lastName, addedChild.lastName(), "Last name should match");
        assertNull(addedChild.birthDate(), "Child with NULL birthDate should excist");

    }

    @Test
    @DisplayName("Should update an existing child")
    void updateShouldUpdateExistingChild() throws SQLException {
        // Arrange
        Child child = new Child("Merlin", "Monro", LocalDate.of(2005, 5, 12));
        Child addedChild = db.addChild(child);

        System.out.println("[DEBUG_LOG] Added child ID: " + addedChild.id());

        Child updatedChild = new Child(addedChild.id(),"Jeremi","Poker",
                LocalDate.of(2007, 12, 13));
        // Act
        boolean isUpdated = db.updateChild(updatedChild);

        System.out.println("[DEBUG_LOG] Updated child ID: " + addedChild.id());
        // Assert
        assertTrue(isUpdated, "Child should be updated with status true");
        // Verify
        Child found = db.findById(addedChild.id());

        assertNotNull(found, "Updated child should exist");
        assertEquals(addedChild.id(), found.id());
        assertEquals("Jeremi", found.firstName(), "Name should be updated");
        assertEquals("Poker", found.lastName(), "Secondname should be updated");
        assertEquals(LocalDate.of(2007, 12, 13), found.birthDate(), "BirthDate should be updated ");
    }

    @Test
    @DisplayName("Throw IllegalArgumentException when updated child is null")
    void shouldThrowException_whenUpdatedChildisNull() throws SQLException {
        //Act and Assert
        assertThrows(IllegalArgumentException.class, () -> db.updateChild(null),
                "Should throw IllegalArgumentException when updated child is null");
    }

    @Test
    @DisplayName("Throw IllegalArgumentException when updated child id is null")
    void shouldThrowException_whenUpdatedChildWithNullid() {
        //Arrange
        Child updateChild = new Child(null,"Tom", "Perk",LocalDate.of(2005, 10, 11));
        //Act and Assert
        assertThrows(IllegalArgumentException.class, () -> db.updateChild(updateChild),
                "Updated child id cant be null");
    }

    @Test
    @DisplayName("Throw IllegalArgumentException when updated child firstname is null")
    void shouldThrowException_whenUpdatedChildWithNullFirstName() throws SQLException {
        // Arrange
        // Add new child
        Child child = new Child("John","Doe",LocalDate.of(2012, 10, 5));
        Child addedChild = db.addChild(child);
        // Creating updating child with null firstName
        Child invalidChild = new Child(addedChild.id(),null,"Porker",LocalDate.of(2008, 10, 11));
        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> db.updateChild(invalidChild),
                "Updated child firstname cant be null");
    }
    @Test
    @DisplayName("Throw IllegalArgumentException when updated child lastname is null")
    void shouldThrowException_whenUpdatedChildWithNullLastName() throws SQLException {
        // Arrange
        // Add new child
        Child child = new Child("Poter","Koker",LocalDate.of(2012, 10, 5));
        Child addedChild = db.addChild(child);
        // Creating updating child with null firstName
        Child invalidChild = new Child(addedChild.id(),"Panda",null,LocalDate.of(2008, 10, 11));
        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> db.updateChild(invalidChild),
                "Updated child lastname cant be null");
    }

    @Test
    @DisplayName("Should throw SQLException when updating with closed connection")
    void shouldThrowSQLException_whenUpdateWithClosedConnection() throws Exception {
        // Arrange
        Connection conn = DBUtil.getConnection();
        conn.close();

        ChildDB brokenDb = new ChildDB(conn);

        Child child = new Child(1L,"John","Doe",LocalDate.of(2010, 1, 1));

        // Act + Assert
        SQLException ex = assertThrows(SQLException.class,() -> brokenDb.updateChild(child),
                "SQLException must appear");

        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Should delete an existing child")
    void deleteShouldDeleteExistingChild() throws SQLException {
        // Arrange - Add a child first
        Child newChild = new Child("Poker", "Shmidt",
                LocalDate.of(2008, 10, 11));

        Child addedChild = db.addChild(newChild);

        System.out.println("[DEBUG_LOG] Added child ID: " + addedChild.id());
        // Act
        boolean deletingChild = db.deleteChild(addedChild.id());

        System.out.println("[DEBUG_LOG] Deleting child ID: " + addedChild.id());
        // Assert
        assertTrue(deletingChild, "Child should be deleted successfully");
        // Verify the deletion by querying the database
        Child deletedChild = db.findById(addedChild.id());

        assertNull(deletedChild, "Deleted child should not exist");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when delete id is null")
    void shouldThrowException_whenDeleteIdIsNull() {
        //Assert
        assertThrows(IllegalArgumentException.class,() -> db.deleteChild(null),
                "Cant delete child witout id");
    }

    @Test
    @DisplayName("Should throw SQLException when deleting with closed connection")
    void shouldThrowSQLException_whenDeleteWithClosedConnection() throws Exception {
        //Act
        Connection conn = DBUtil.getConnection();
        conn.close();

        ChildDB brokenDb = new ChildDB(conn);
        //Assert
        assertThrows(SQLException.class,
                () -> brokenDb.deleteChild(1L),"should throw SQLException when connection is closed");
    }


    @Test
    @DisplayName("Should return children with at least the specified age")
    void findChildrenWithMinimumAgeShouldReturnChildrenWithMinimumAge() throws SQLException {
        // Arrange - Add children with different ages
        // Child 1 - 10 years old
        Child child1 = new Child("John", "Amsterdam", LocalDate.of(2018,11,15));
        // Child 2 - 5 years old
        Child child2 = new Child("Motan", "Polius", LocalDate.of(2022,10,15));
        // Child 3 - 15 years old
        Child child3 = new Child("Petr", "Skoroda", LocalDate.of(2012,11,15));

        Child addFirstChild = db.addChild(child1);
        System.out.println("[DEBUG_LOG] Added first child ID: " + addFirstChild.id());

        Child addSecondChild = db.addChild(child2);
        System.out.println("[DEBUG_LOG] Added second child ID: " + addSecondChild.id());

        Child addThirdChild = db.addChild(child3);
        System.out.println("[DEBUG_LOG] Added third child ID: " + addThirdChild.id());
        // Act - Get all children at least 10 years old
        int minimumAge = 10;
        List<Child> listOfFoundChildren = db.findChildrenWithMinimumAge(minimumAge);

        System.out.println("[DEBUG_LOG] Found children with minimum age: " + minimumAge);
        // Assert
        assertNotNull(listOfFoundChildren);
        // Verify that the result contains children with correct ages
        for (Child child : listOfFoundChildren) {

            int age = java.time.Period.between(child.birthDate(), LocalDate.now()).getYears();

            assertTrue(age >= minimumAge,"Child age should be >= " + minimumAge);
        }

    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when age is negative")
    void shouldThrowException_whenAgeIsNegative() {

        assertThrows(IllegalArgumentException.class, () -> db.findChildrenWithMinimumAge(-1),
                "Cant be minimum age < 0");
    }

    @Test
    @DisplayName("Should throw SQLException when connection is closed")
    void shouldThrowSQLException_whenConnectionClosed() throws Exception {

        Connection conn = DBUtil.getConnection();
        conn.close();

        ChildDB brokenDb = new ChildDB(conn);

        assertThrows(SQLException.class, () -> brokenDb.findChildrenWithMinimumAge(10),
                "should throw SQLException when connection is closed");
    }

    @Test
    @DisplayName("Should return children with null birth date")
    void findChildrenWithoutBirthDateShouldReturnChildrenWithNullBirthDate() throws SQLException {
        // Arrange - Add children with and without birth dates
        // Child with birth date
        Child childWithBirthDate = new Child("Jesica", "Onil",
                LocalDate.of(2010, 10, 11));

        Child addChildWithBirthDate = db.addChild(childWithBirthDate);
        System.out.println("[DEBUG_LOG] Added child with BirthDate ID: " + addChildWithBirthDate.id());
        // Child without birth date
        Child childWithoutBirthDate = new Child("Jesica", "Onil", null);

        Child addChildWithoutBirthDate = db.addChild(childWithoutBirthDate);
        System.out.println("[DEBUG_LOG] Added child without BirthDate ID: " + addChildWithoutBirthDate.id());
        // Act
        List<Child> result = db.findChildrenWithoutBirthDate();
        // Assert
        assertNotNull(result, "should return the List of children without BirthDtae");
        for (Child child : result) {
            assertNull(child.birthDate(), "All returned children must have null birthDate");
        }
        List<Long> ids = result.stream()
                .map(Child::id)
                .toList();

        assertTrue(ids.contains(addChildWithoutBirthDate.id()));

    }
}

