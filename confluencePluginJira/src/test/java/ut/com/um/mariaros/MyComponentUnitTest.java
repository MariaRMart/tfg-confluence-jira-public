package ut.com.um.mariaros;

import org.junit.Test;
import com.um.mariaros.api.MyPluginComponent;
import com.um.mariaros.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}