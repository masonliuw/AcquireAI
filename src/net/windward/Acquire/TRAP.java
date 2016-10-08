/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE"
 * As long as you retain this notice you can do whatever you want with this
 * stuff. If you meet an employee from Windward some day, and you think this
 * stuff is worth it, you can buy them a beer in return. Windward Studios
 * ----------------------------------------------------------------------------
 */

package net.windward.Acquire;

/**
 Used to set code coverage breakpoints in the code in DEBUG mode only.
*/
public class TRAP extends RuntimeException {

	public static boolean debugMode = true;

	/**
	 * Throws us into the debugger.
	 */
	static public void trap() {

		if (! debugMode)
			return;

		// hit the debugger
		try { throw new TRAP(); } catch (TRAP tr) { /*empty*/ } }

	static public void trap(boolean doBreak) {

		if (! debugMode)
			return;

		// hit the debugger
		try { if (doBreak) throw new TRAP(); } catch (TRAP tr) { /*empty*/ } }
}