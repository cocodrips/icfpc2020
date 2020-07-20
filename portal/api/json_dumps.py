import json
def default_method(item):
    if isinstance(item, object) and hasattr(item, '__dict__'):
        return item.__dict__
    else:
        raise TypeError

def dumps(obj):
    return json.dumps(obj, default=default_method, indent=2)

if __name__ == '__main__':
    from api.replayer import response_parser

    raw_data = '[1,1,[256,0,[512,1,64],[16,128],[97,47,11,1]],[0,[16,128],[[[1,0,(-29,48),(0,0),[97,47,11,1],0,64,1],nil],[[0,1,(29,-48),(0,0),[64,64,10,1],0,64,1],nil]]]]'
    obj = response_parser.parse(raw_data)
    print(dumps(obj))
