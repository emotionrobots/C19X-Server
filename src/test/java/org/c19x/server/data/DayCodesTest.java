package org.c19x.server.data;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;

import org.junit.Test;

public class DayCodesTest {

	// 0:0 day=1443087401359677750, seed=7525003092670007258,
	// code=-8129047106903552267
	// 0:1 day=1443087401359677750, seed=7525003092670007258,
	// code=8443977219705625829
	// 1:0 day=7940984811893783192, seed=3537033893445398299,
	// code=-850124991640589653
	// 1:1 day=7940984811893783192, seed=3537033893445398299,
	// code=-5720254713763274613
	@Test
	public void dayCodes() {
		final byte[] secret = new byte[] { 0 };
		final long[] dayCodes = DayCodes.dayCodes(secret, 2);
		for (int i = 0; i < dayCodes.length; i++) {
			final long dayCode = dayCodes[i];
			final long beaconCodeSeed = DayCodes.beaconCodeSeed(dayCode);
			final long[] beaconCodes = DayCodes.beaconCodes(beaconCodeSeed, 2);
			for (int j = 0; j < beaconCodes.length; j++) {
				final long beaconCode = beaconCodes[j];
				System.err.println(i + ":" + j + " dayCode=" + dayCode + ", beaconCodeSeed=" + beaconCodeSeed
						+ ", beaconCode=" + beaconCode);
			}
		}
	}

	// day=0, dayCode=1443087401359677750, beaconCodeSeed=7525003092670007258,
	// beaconCodes=[-8129047106903552267, 8443977219705625829]
	// day=1, dayCode=7940984811893783192, beaconCodeSeed=3537033893445398299,
	// beaconCodes=[-850124991640589653, -5720254713763274613]
	@Test
	public void todayCodes() {
		final byte[] secret = new byte[] { 0 };
		final long[] dayCodes = DayCodes.dayCodes(secret, 2);
		final long[] todayDayCodes = DayCodes.getDayCodes(dayCodes, 0, 2);
		final long[] todayBeaconCodeSeeds = DayCodes.getBeaconCodeSeeds(dayCodes, 0, 2);
		assertEquals(1443087401359677750l, todayDayCodes[0]);
		assertEquals(7525003092670007258l, todayBeaconCodeSeeds[0]);
		assertEquals(7940984811893783192l, todayDayCodes[1]);
		assertEquals(3537033893445398299l, todayBeaconCodeSeeds[1]);
		System.err.println("day=0, dayCode=" + todayDayCodes[0] + ", beaconCodeSeed=" + todayBeaconCodeSeeds[0]
				+ ", beaconCodes=" + Arrays.toString(DayCodes.beaconCodes(todayBeaconCodeSeeds[0], 2)));
		System.err.println("day=1, dayCode=" + todayDayCodes[1] + ", beaconCodeSeed=" + todayBeaconCodeSeeds[1]
				+ ", beaconCodes=" + Arrays.toString(DayCodes.beaconCodes(todayBeaconCodeSeeds[1], 2)));
	}

	// SERVER
	// day=153, dayCode=-7760134536738241307, beaconCodeSeed=-6483623051771494729,
	// beaconCodes=[5569969605707727818, -2638397902879369169]
	// iOS
	// 153:0 day=-7760134536738241307, seed=-6483623051771494729,
	// code=5569969605707727818
	// 153:1 day=-7760134536738241307, seed=-6483623051771494729,
	// code=-2638397902879369169
	@Test
	public void day153Codes() {
		final byte[] secret = new byte[] { 0 };
		final long[] dayCodes = DayCodes.dayCodes(secret, 365 * 5);
		final int today = 153;
		final long[] todayDayCodes = DayCodes.getDayCodes(dayCodes, today, today + 1);
		final long[] todayBeaconCodeSeeds = DayCodes.getBeaconCodeSeeds(dayCodes, today, today + 1);
		assertEquals(-7760134536738241307l, todayDayCodes[0]);
		assertEquals(-6483623051771494729l, todayBeaconCodeSeeds[0]);
		System.err.println("day=153, dayCode=" + todayDayCodes[0] + ", beaconCodeSeed=" + todayBeaconCodeSeeds[0]
				+ ", beaconCodes=" + Arrays.toString(DayCodes.beaconCodes(todayBeaconCodeSeeds[0], 2)));
	}

	@Test
	public void getDayCodes() {
		assertEquals(0, DayCodes.getDayCodes(new long[] { 0, 1, 2 }, 0, 0).length);
		assertEquals(0, DayCodes.getDayCodes(new long[] { 0, 1, 2 }, 0, 1)[0]);
		assertEquals(1, DayCodes.getDayCodes(new long[] { 0, 1, 2 }, 1, 2)[0]);
	}

	@Test
	public void infectionData() {
		final File folder = new File("tmp");
		if (folder.exists()) {
			for (final File file : folder.listFiles()) {
				file.delete();
			}
		} else {
			folder.mkdir();
		}
		final Devices devices = new Devices(folder);
		final String registration = devices.register("1", new byte[] { 0 });
		assertEquals("1,AA==", registration);

		devices.setStatus("1", "2");
		assertEquals("2", devices.getStatus("1"));

		final DayCodes deviceDayCodes = devices.getCodes("1");
		final int today = 153;
		final long[] todayDayCodes = deviceDayCodes.getDayCodes(today, today + 1);
		final long[] todayBeaconCodeSeeds = deviceDayCodes.getBeaconCodeSeeds(today, today + 1);
		assertEquals(-7760134536738241307l, todayDayCodes[0]);
		assertEquals(-6483623051771494729l, todayBeaconCodeSeeds[0]);
		final InfectionData infectionData = new InfectionData(devices, 1);
		assertEquals("{\"-6483623051771494729\":\"2\"}", infectionData.toJSON());
		System.err.println(infectionData.toJSON());
	}
}
