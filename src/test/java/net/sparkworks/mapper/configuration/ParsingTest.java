package net.sparkworks.mapper.configuration;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

@Slf4j
public class ParsingTest {
    
    @Test
    public void testParseComplexMessage() {
        final List<MqttConfiguration.ParsedReading> readings = MqttConfiguration.parseComplexMessage("0013a200409c1683", "0xd19/pir,0");
        Assert.assertNotNull(readings);
        Assert.assertEquals(1, readings.size());
    }
    
    @Test
    public void testParseComplexMessage1() {
        final List<MqttConfiguration.ParsedReading> readings = MqttConfiguration.parseStringMessage("0013a200409c1683", "0xd19/pir,0+");
        Assert.assertNotNull(readings);
        Assert.assertEquals(1, readings.size());
    }
    
    @Test
    public void testParseComplexMessage2() {
        final List<MqttConfiguration.ParsedReading> readings = MqttConfiguration.parseStringMessage("0013a20040a1d47f", "0xc55/cur/1,8069+cur/2,13804+cur/3,420+");
        Assert.assertNotNull(readings);
        Assert.assertEquals(3, readings.size());
    }
    
    @Test
    public void testParseComplexMessage3() {
        final List<MqttConfiguration.ParsedReading> readings = MqttConfiguration.parseStringMessage("0013a20040a1d47f", "0xa97/temp,18.36+light,75+pir,0+sound,61+humid,49.40+");
        Assert.assertNotNull(readings);
        Assert.assertEquals(5, readings.size());
    }
    
    @Test
    public void testParseComplexMessage4() {
        final List<MqttConfiguration.ParsedReading> readings = MqttConfiguration.parseStringMessage("dragino-191078", "4/temp,22.90+humid,96.50+light,57+sound,47+pir,0+vcc,5051+rssin,-95+snrn,-56+rssi,-88+snr,-55+pl,58+");
        Assert.assertNotNull(readings);
        Assert.assertEquals(11, readings.size());
    }
    
    @Test
    public void testParseComplexMessage5() {
        final List<MqttConfiguration.ParsedReading> readings = MqttConfiguration.parseStringMessage("dragino-18ea50", "8/cur/1,50811.49+cur/2,378.16+cur/3,911.14+rssi,-50+");
        Assert.assertNotNull(readings);
        Assert.assertEquals(4, readings.size());
    }
}