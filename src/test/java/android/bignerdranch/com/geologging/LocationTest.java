package android.bignerdranch.com.geologging;

import android.content.Context;
import android.location.Location;
import android.test.InstrumentationTestCase;

import com.firebase.client.Firebase;
import com.indooratlas.android.sdk.IALocation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Created by Sheena on 07/04/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class LocationTest {
    @Mock
    Context mMockedContext;

    @Test
    public void LocationCoordsTest(){
        MainActivity activity = Mockito.mock(MainActivity.class);
        Location TestLocation = new Location("TestLocation");
        when(activity.GetCoords(TestLocation)).thenReturn((float) 5.0);
        assertThat(activity.GetCoords(TestLocation),is((float) 5.0));
    }

    @Test
    public void TestFirebaseStore(){
        MainActivity activity = Mockito.mock(MainActivity.class);
        Firebase.setAndroidContext(activity);
        Firebase fstore = new Firebase("https://mybirkbeck-4ca74.firebaseio.com/");
        fstore.child("TestSave").setValue("Do you have data? You'll love Firebase.");
    }
}
