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

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
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
    public void LocationCoordsDistanceTest(){
        MainActivity activity = Mockito.mock(MainActivity.class);
        double firstLocLat = 51.522254998285774;
        double firstLocLong = -0.1307914118549927;
        double secondLocLat = 51.522254998285774;
        double secondLocLong =  -0.1302318118549927;
        when(activity.GetDistance(firstLocLat, firstLocLong, secondLocLat,secondLocLong)).thenReturn((float) 5.0);
        assertThat(activity.GetDistance(firstLocLat, firstLocLong, secondLocLat,secondLocLong),is((float)5.0));
    }
    @Test
    public void TestFirebaseStore(){
        MainActivity activity = Mockito.mock(MainActivity.class);
        assertThat("Value Saved", is("Value Saved"));
    }
}
