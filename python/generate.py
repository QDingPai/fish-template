def build_day_list(from_date, to_date):
    # 将输入日期字符串转换为 datetime 对象
    from_date = datetime.strptime(from_date, '%Y-%m-%d')
    to_date = datetime.strptime(to_date, '%Y-%m-%d')

    # 初始化日期列表
    day_list = []

    # 生成日期范围内的日期
    current_date = from_date
    while current_date <= to_date:
        day_list.append(current_date.strftime('%Y-%m-%d %H:%M:%S'))  # 添加时间部分
        current_date += timedelta(days=1)

    return day_list


def build_query_data_list(exchange):
    # 参数配置区域
    symbols_list = ['ETH/USDT', 'EOS/USDT', 'LTC/USDT','BTC/USDT']
    time_intervals_list = ['5m', '15m']
    from_data = '2023-09-01'
    to_data = '2023-10-15'
    day_list = build_day_list(from_data, to_data)

    data_list = []
    for symbol in symbols_list:
        for time_interval in time_intervals_list:
            for current_day in day_list:
                data = TradingData('E:\DB', exchange, symbol, time_interval, current_day, '' , '')
                data_list.append(data)

    return data_list