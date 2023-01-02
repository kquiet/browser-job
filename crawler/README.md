# Crawler

This project is an experiment project to scrape various websites for later automation.

There are multiple browser jobs:

|Job|Description|
|---|---|
|`Sale591`|Scrape [591/sale][] and notify through Telegram|

Before running any job, please refer to [Browser-Scheduler][] for the details of setting a browser job.

## Sale591

This job will read job settings from a SQL database(MySQL currently), scrape [591/sale][], and then send messages to the desired chat through [Telegram Bot API][].

### Usage
For this job, you need to prepare a MySQL/MariaDb database to store its settings in table `botconfig`.
There are three columns defined in `botconfig`:
|Column|Description|
|---|---|
|`botname`|bot name; it's derived from the job name|
|`key`|config name|
|`value`|config value|

Necessary settings:
|key|Description|
|---|---|
|`entryUrl`|The target URL of [591/sale][] you are going to scrape|
|`chatId`|The number shown in the URL of the desired chat after logging in [Telegram Web][]|
|`chatToken`|The authentication token when your Telegram Bot is created through [Telegram BotFather][]|

After the above settings is done, just invite the created Telegram Bot to the desired chat, then it's all set.

As this job adopts [Flyway][], you can write your own script files(.sql) and place them under path `db/migration/` to let Flyway establish the settings for your instead of manually creating them through other SQL tools.


[591/sale]: https://sale.591.com.tw/ "591 sale"
[Telegram Bot API]: https://core.telegram.org/bots/api "Telegram Bot API"
[Telegram Web]: https://web.telegram.org/z "Telegram Web"
[Telegram BotFather]: https://core.telegram.org/bots/features#botfather "Telegram BotFather"
[Browser-Scheduler]: https://github.com/kquiet/browser-scheduler "Browser-Scheduler"
[Flyway]: https://flywaydb.org/documentation/