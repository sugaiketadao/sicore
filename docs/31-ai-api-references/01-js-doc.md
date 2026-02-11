# JavaScript API Reference

## DomUtil

### Methods

#### isExists

```
isExists(elms)
```

**Parameters**:
- `elms`: `Element|NodeList`

**Returns**: `boolean`

#### isVisible

```
isVisible(elm)
```

**Parameters**:
- `elm`: `Element`

**Returns**: `boolean`

#### getById

```
getById(id, outerElm)
```

**Parameters**:
- `id`: `string`
- `[outerElm]`: `Object`

**Returns**: `Element|null`

#### getSelector

```
getSelector(selector, outerElm)
```

**Parameters**:
- `selector`: `string`
- `[outerElm]`: `Object`

**Returns**: `Element|null`

#### getByName

```
getByName(name, outerElm)
```

**Parameters**:
- `name`: `string`
- `[outerElm]`: `Object`

**Returns**: `Element|null`

#### getByDataName

```
getByDataName(name, outerElm)
```

**Parameters**:
- `name`: `string`
- `[outerElm]`: `Object`

**Returns**: `Element|null`

#### getsSelector

```
getsSelector(selector, outerElm)
```

**Parameters**:
- `selector`: `string`
- `[outerElm]`: `Object`

**Returns**: `Array<Element>|null`

#### getParentByTag

```
getParentByTag(baseElm, tag)
```

**Parameters**:
- `baseElm`: `Element`
- `tag`: `string`

**Returns**: `Element|null`

#### getVal

```
getVal(elm)
```

**Parameters**:
- `elm`: `Element`

**Returns**: `string|null`

#### setVal

```
setVal(elm, value)
```

**Parameters**:
- `elm`: `Element`
- `value`: `string`

**Returns**: `boolean`

#### getTxt

```
getTxt(elm)
```

**Parameters**:
- `elm`: `Element`

**Returns**: `string|null`

#### setTxt

```
setTxt(elm, text)
```

**Parameters**:
- `elm`: `Element`
- `text`: `string`

**Returns**: `boolean`

#### setEnable

```
setEnable(elm, isEnable)
```

**Parameters**:
- `elm`: `Element`
- `isEnable`: `boolean|string`

**Returns**: `boolean`

#### setVisible

```
setVisible(elm, isShow, keepLayout)
```

**Parameters**:
- `elm`: `Element`
- `isShow`: `boolean|string`
- `keepLayout`: `boolean`

**Returns**: `boolean`

#### getAttr

```
getAttr(elm, attrName)
```

**Parameters**:
- `elm`: `Element`
- `attrName`: `string`

**Returns**: `string|number|null`

#### setAttr

```
setAttr(elm, attrName, val)
```

**Parameters**:
- `elm`: `Element`
- `attrName`: `string`
- `val`: `string`

**Returns**: `boolean`

#### hasAttr

```
hasAttr(elm, attrName)
```

**Parameters**:
- `elm`: `Element`
- `attrName`: `string`

**Returns**: `boolean`

#### removeAttr

```
removeAttr(elm, attrName)
```

**Parameters**:
- `elm`: `Element`
- `attrName`: `string`

**Returns**: `boolean`

#### addClass

```
addClass(elm, cls)
```

**Parameters**:
- `elm`: `Element`
- `cls`: `string`

**Returns**: `boolean`

#### removeClass

```
removeClass(elm, cls)
```

**Parameters**:
- `elm`: `Element`
- `cls`: `string`

**Returns**: `boolean`

#### hasClass

```
hasClass(elm, cls)
```

**Parameters**:
- `elm`: `Element`
- `cls`: `string`

**Returns**: `boolean`

## FrmUtil

### Methods

#### num

```
num(value)
```

**Parameters**:
- `value`: `string`

**Returns**: `string`

#### ymd

```
ymd(value)
```

**Parameters**:
- `value`: `string`

**Returns**: `string`

#### hms

```
hms(value)
```

**Parameters**:
- `value`: `string`

**Returns**: `string`

## HttpUtil

### Methods

#### convUrlParam

```
convUrlParam(obj)
```

**Parameters**:
- `obj`: `Object.<string,string>`

**Returns**: `string`

#### getUrlParams

```
getUrlParams()
```

**Returns**: `Object`

#### movePage

```
movePage(url, params)
```

**Parameters**:
- `url`: `string`
- `[params]`: `Object.<string, string>|string`

## PageUtil

### Methods

#### setMsg

```
setMsg(res)
```

**Parameters**:
- `res`: `Object`

#### hasError

```
hasError(res)
```

**Parameters**:
- `res`: `Object`

**Returns**: `boolean`

#### clearMsg

```
clearMsg()
```

#### getValues

```
getValues(outerElm)
```

**Parameters**:
- `[outerElm]`: `Object`

**Returns**: `Object`

#### getRowValues

```
getRowValues(rowElm)
```

**Parameters**:
- `rowElm`: `Element`

**Returns**: `Object`

#### getRowValuesByInnerElm

```
getRowValuesByInnerElm(baseElm, rowTag)
```

**Parameters**:
- `baseElm`: `Element`
- `[rowTag]`: `string`

**Returns**: `Object`

#### setValues

```
setValues(obj, outerElm)
```

**Parameters**:
- `obj`: `Object`
- `[outerElm]`: `Element`

#### addRow

```
addRow(listId, obj)
```

**Parameters**:
- `listId`: `string`
- `[obj]`: `Object|Array<Object>`

#### removeRow

```
removeRow(searchElmName, searchElmVal, rowTag)
```

**Parameters**:
- `searchElmName`: `string`
- `searchElmVal`: `string`
- `[rowTag]`: `string`

**Returns**: `boolean`

#### clearRows

```
clearRows(listId)
```

**Parameters**:
- `listId`: `string`

## StorageUtil

### Methods

#### getPageObj

```
getPageObj(key, notExistsValue)
```

**Parameters**:
- `key`: `string`
- `[notExistsValue]`: `Object`

**Returns**: `Object|null`

#### getModuleObj

```
getModuleObj(key, notExistsValue)
```

**Parameters**:
- `key`: `string`
- `[notExistsValue]`: `Object`

**Returns**: `Object|null`

#### getSystemObj

```
getSystemObj(key, notExistsValue)
```

**Parameters**:
- `key`: `string`
- `[notExistsValue]`: `Object`

**Returns**: `Object|null`

#### setPageObj

```
setPageObj(key, obj)
```

**Parameters**:
- `key`: `string`
- `obj`: `Object`

**Returns**: `boolean`

#### setModuleObj

```
setModuleObj(key, obj)
```

**Parameters**:
- `key`: `string`
- `obj`: `Object`

**Returns**: `boolean`

#### setSystemObj

```
setSystemObj(key, obj)
```

**Parameters**:
- `key`: `string`
- `obj`: `Object`

**Returns**: `boolean`

#### removePage

```
removePage(key)
```

**Parameters**:
- `key`: `string`

**Returns**: `boolean`

#### removeModule

```
removeModule(key)
```

**Parameters**:
- `key`: `string`

**Returns**: `boolean`

#### removeSystem

```
removeSystem(key)
```

**Parameters**:
- `key`: `string`

**Returns**: `boolean`

#### clearAllData

```
clearAllData()
```

**Returns**: `boolean`

#### clearPage

```
clearPage()
```

**Returns**: `boolean`

#### clearModule

```
clearModule()
```

**Returns**: `boolean`

#### clearSystem

```
clearSystem()
```

**Returns**: `boolean`

## UnFrmUtil

### Methods

#### num

```
num(value)
```

**Parameters**:
- `value`: `string`

**Returns**: `string`

#### ymd

```
ymd(value)
```

**Parameters**:
- `value`: `string`

**Returns**: `string`

#### hms

```
hms(value)
```

**Parameters**:
- `value`: `string`

**Returns**: `string`

## ValUtil

### Methods

#### isNull

```
isNull(obj)
```

**Parameters**:
- `obj`: `Object`

**Returns**: `boolean`

#### isBlank

```
isBlank(str)
```

**Parameters**:
- `str`: `string`

**Returns**: `boolean`

#### nvl

```
nvl(value, rep)
```

**Parameters**:
- `value`: `string`
- `[rep]`: `string`

**Returns**: `string`

#### bvl

```
bvl(value, rep)
```

**Parameters**:
- `value`: `string`
- `rep`: `string`

**Returns**: `string`

#### substring

```
substring(value, beginIndex, endIndex)
```

**Parameters**:
- `value`: `string`
- `[beginIndex]`: `number`
- `[endIndex]`: `number`

**Returns**: `string`

#### equals

```
equals(val1, val2)
```

**Parameters**:
- `val1`: `string`
- `val2`: `string`

**Returns**: `boolean`

#### equalsObj

```
equalsObj(obj1, obj2, ignoreKeys)
```

**Parameters**:
- `obj1`: `Object`
- `obj2`: `Object`
- `ignoreKeys`: `string`

**Returns**: `boolean`

#### isEmpty

```
isEmpty(obj)
```

**Parameters**:
- `obj`: `Object`

**Returns**: `boolean`

#### isNum

```
isNum(value, minusNg, decNg)
```

**Parameters**:
- `value`: `string`
- `[minusNg]`: `boolean`
- `[decNg]`: `boolean`

**Returns**: `boolean`

#### isDate

```
isDate(yyyymmdd)
```

**Parameters**:
- `yyyymmdd`: `string`

**Returns**: `boolean`

#### isTrue

```
isTrue(val)
```

**Parameters**:
- `val`: `string|boolean`

**Returns**: `boolean`

#### toDate

```
toDate(yyyymmdd)
```

**Parameters**:
- `yyyymmdd`: `string`

**Returns**: `Date`

#### dateTo

```
dateTo(dateObj)
```

**Parameters**:
- `dateObj`: `Date`

**Returns**: `string`

#### isAry

```
isAry(obj)
```

**Parameters**:
- `obj`: `Object`

**Returns**: `boolean`

#### isObj

```
isObj(obj)
```

**Parameters**:
- `obj`: `Object`

**Returns**: `boolean`

#### lpad

```
lpad(value, pad, len)
```

**Parameters**:
- `value`: `string`
- `pad`: `string`
- `len`: `number`

**Returns**: `string`

#### rpad

```
rpad(value, pad, len)
```

**Parameters**:
- `value`: `string`
- `pad`: `string`
- `len`: `number`

**Returns**: `string`

#### toType

```
toType(obj)
```

**Parameters**:
- `obj`: `Object`

**Returns**: `string`

