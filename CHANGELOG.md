Changelog
=========

Version 1.0.0
--------------------
*   Complete rework of the project, from the ground up
*   Includes both local and global warmups and cooldowns
*   More configuration, more stability, more power
*   Verbose localizations, including custom status messages per command

Version 0.5.0
-------------
*   Massive restructuring of the plugin, which included moving it to Maven.
*   The configuration file has dropped in complexity, yet offers most of the same functionality.
*   Local cooldowns are simply not realistic. As such, I've made commands local in the sense that only commands listed for the user in my configuration will be cooled or warmed.
*   All cooldowns are global. If a command has a 10 second cooldown, all commands [registered to my plugin] are unusable for 10 seconds.
*   Removed the ability to cancel warmups by moving. If I find some way to easily configure options for this, I'll add it back in.

Version 0.4.0a
--------------
*   Localizations for every conceivable command variation
*   Warm ups and cooldowns can be interrupted by damage, and can work on different timers
*   Won't claim anything outlandish like it's stable, as this is a massive amount of work and I can't really test it all on my own.
*   Hopefully you're satisfied!
