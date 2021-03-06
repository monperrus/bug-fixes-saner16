package models;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.Ignore;

import com.avaje.ebean.Page;
import play.data.validation.Validation;

public class UserTest extends ModelTest<User> {

    @Test
    public void save() throws Exception {
        User user = new User();

        user.loginId="foo";
        assertThat(Validation.getValidator().validate(user).size()).describedAs("'foo' should be accepted.").isEqualTo(0);

        user.loginId=".foo";
        assertThat(Validation.getValidator().validate(user).size()).describedAs("'.foo' should NOT be accepted.").isGreaterThan(0);

        user.loginId="foo.bar";
        assertThat(Validation.getValidator().validate(user).size()).describedAs("'foo.bar' should be accepted.").isEqualTo(0);

        user.loginId="foo.";
        assertThat(Validation.getValidator().validate(user).size()).describedAs("'foo.' should NOT be accepted.").isGreaterThan(0);

        user.loginId="_foo";
        assertThat(Validation.getValidator().validate(user).size()).describedAs("'_foo' should NOT be accepted.").isGreaterThan(0);

        user.loginId="foo_bar";
        assertThat(Validation.getValidator().validate(user).size()).describedAs("'foo_bar' should be accepted.").isEqualTo(0);

        user.loginId="foo_";
        assertThat(Validation.getValidator().validate(user).size()).describedAs("'foo_' should NOT be accepted.").isGreaterThan(0);

        user.loginId="-foo";
        assertThat(Validation.getValidator().validate(user).size()).describedAs("'-foo' should be accepted.").isEqualTo(0);

        user.loginId="foo-";
        assertThat(Validation.getValidator().validate(user).size()).describedAs("'foo-' should be accepted.").isEqualTo(0);
    }

	@Test
	public void findById() throws Exception {
		// Given
		// When
		User user = User.find.byId(2l);
		// Then
		assertThat(user.name).isEqualTo("Hobi");
	}

	@Test
	public void findByName() throws Exception {
		// Given
		// When
		User user = User.findByName("Hobi");
		// Then
		assertThat(user.id).isEqualTo(2l);
	}

	@Test
    public void findNameById() throws Exception {
        //Given
        //When
	    String name = User.find.byId(2l).name;
        //Then
	    assertThat(name).isEqualTo("Hobi");
    }
	
	@Test
    public void options() throws Exception {
        // Given
        // When
        Map<String, String> userOptions = User.options();
        // Then
        assertThat(userOptions).hasSize(5);
    }

	@Test
	public void findByLoginId() throws Exception {
	    // Given
	    // When
	    User user = User.findByLoginId("k16wire");
	    // Then
	    assertThat(user.id).isEqualTo(3l);
	}

	@Test
	public void findUsers() throws Exception {
	    // Given
	    // When
	    Page<User> searchUsers = User.findUsers(0, "ho");
	    // Then
	    assertThat(searchUsers.getTotalRowCount()).isEqualTo(1);
	}

	@Test
	public void findProjectsById() throws Exception {
	    // Given
	    // When
	    User user = User.findProjectsById(2l);
	    // Then
	    assertThat(user.projectUser.size()).isEqualTo(2);
	    assertThat(user.projectUser.iterator().next().project.name).isEqualTo("nForge4java");
	}

	@Test
    public void findUsersByProject() throws Exception {
        // Given
        // When
        List<User> users = User.findUsersByProject(2l);
        // Then
        assertThat(users.size()).isEqualTo(2);
    }
	
	@Test
	public void isLoginId() throws Exception {
	    // Given
	    String existingId = "hobi";
	    String nonExistingId = "hobiii";
	    // When
	    boolean result1 = User.isLoginId(existingId);
	    boolean result2 = User.isLoginId(nonExistingId);
	    boolean result3 = User.isLoginId(null);
	    // Then
	    assertThat(result1).isEqualTo(true);
	    assertThat(result2).isEqualTo(false);
	    assertThat(result3).isEqualTo(false);
	}

    @Test
    public void isEmailExist() throws Exception {
        // Given
        String expectedUser = "doortts@gmail.com";

    	// When // Then
        assertThat(User.isEmailExist(expectedUser)).isTrue();
    }

    @Test
    public void isEmailExist_nonExist() throws Exception {
        // Given
        String expectedEmail = "nekure@gmail.com";

        // When // Then
        assertThat(User.isEmailExist(expectedEmail)).isFalse();
    }

}
