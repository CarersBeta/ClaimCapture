#Util functions so the code looks cleaner

#vss stands for validation-summary selector
vss = (href) -> $("div[class='validation-summary'] a[href='##{href}']")
vssText = (href,text)     -> if text? then vss(href).text(text) else if vss(href).length > 0 then vss(href).text().trim() else ""
checked = (selector,val)  -> if val?  then $("##{selector}").prop('checked',val) else $("##{selector}").prop('checked')
val = (selector,text)     -> if text? then $("##{selector}").val(text) else $("##{selector}").val()
subString = (str,start,end) -> str.substring(start.length,end.length)
visible = (selector) -> $("##{selector}").is(":visible")
legend = (selector)-> $("##{selector}").parent().find("legend")
label = (selector) -> $("label[for='#{selector}']")
labelText = (selector,text) ->
  labelChildren = label(selector).children()
  label(selector).text(text).append(labelChildren)
exists = (elem) -> elem? and elem.length > 0
S = (selector) -> $("##{selector}")


window.fixErrorMessages = (o) ->
  pathBeenPaidYet = vss(o.beenPaidYet)
  if exists pathBeenPaidYet
    currentTextBeenPaidYet = pathBeenPaidYet.text().trim()
    existingErrorBeenPaidYet = subString(currentTextBeenPaidYet, o.beenPaidYetText,currentTextBeenPaidYet)
    pathBeenPaidYet.text(o.willBePaidText + existingErrorBeenPaidYet)
  currentText = vssText(o.usuallyPaidSameAmount)
  existingError = subString(currentText, o.usuallyPaidSameAmountText,currentText)
  textToUse = switch (val o.howOftenFrequency)
    when "Weekly" then o.usuallyPaidSameAmountWeekText
    when "Fortnightly" then o.usuallyPaidSameAmountFortnightText
    when "Four-Weekly" then o.usuallyPaidSameAmountFourWeekText
    when "Monthly" then o.usuallyPaidSameAmountMonthText
    else o.usuallyPaidSameAmountOtherText
  vssText(o.usuallyPaidSameAmount,textToUse + existingError)

  pathHowOften = vss(o.howOften)
  if exists pathHowOften
    currentTextHowOften = pathHowOften.text().trim()
    existingErrorHowOften = subString(currentTextHowOften,o.howOftenText,currentTextHowOften)
    pathHowOften.text(o.howOftenWillText + existingErrorHowOften)

  pathHowMuch = vss(o.howMuch)
  if exists pathHowMuch
    currentTextHowMuch = pathHowMuch.text().trim()
    existingErrorHowMuch = currentTextHowMuch.substring(currentTextHowMuch.lastIndexOf(' -'),currentTextHowMuch.length)
    pathHowMuch.text(o.howMuchText + existingErrorHowMuch)


window.beenPaidYet = (beenPaidYetY, beenPaidYetN) ->
  if checked beenPaidYetY
    S("beenPaidYetWrap").slideDown(0).attr 'aria-hidden', 'false'
  else
    S("beenPaidYetWrap").slideUp 0
    S("usuallyPaidSameAmountWrap").slideUp 0

  S(beenPaidYetY).on "click", -> S("beenPaidYetWrap").slideDown(0).attr 'aria-hidden', 'false'

  S(beenPaidYetN).on "click", ->
    S("beenPaidYetWrap").slideUp 0
    S("usuallyPaidSameAmountWrap").slideUp 0
    $("#beenPaidYetWrap input").val("")

window.usuallyPaidSameAmount = (o) ->
  if val(o.howOftenFrequency) is o.dontknowKey and visible "usuallyPaidSameAmountWrap"
    hideUsuallyPaidSameAmountWrap(o)

  S(o.howOftenFrequency).change ->
    if visible "usuallyPaidSameAmountWrap"
      hideUsuallyPaidSameAmountWrap(o)

    if not (val(o.howOftenFrequency)is o.defaultValue or val(o.howOftenFrequency) is "" or val(o.howOftenFrequency) is o.dontknowKey)
      textToUse = switch(val o.howOftenFrequency)
        when "Weekly" then o.usuallyPaidSameAmountWeeklyText
        when "Fortnightly" then o.usuallyPaidSameAmountFortnightlyText
        when "Four-Weelky" then o.usuallyPaidSameAmountFourWeeklyText
        when "Monthly" then o.usuallyPaidSameAmountMonthlyText
        else o.usuallyPaidSameAmountOtherText
      legend(o.usuallyPaidSameAmount).html(textToUse)

      S(o.usuallyPaidSameAmountY).prop('checked', false)
      S(o.usuallyPaidSameAmountN).prop('checked', false)
      S("usuallyPaidSameAmountWrap").slideDown(0).attr 'aria-hidden', 'false'

hideUsuallyPaidSameAmountWrap = (o) ->
  S("usuallyPaidSameAmountWrap").slideUp 0, ->
    S(o.usuallyPaidSameAmountY).prop('checked', false)
    S(o.usuallyPaidSameAmountN).prop('checked', false)

window.whatFor = (payIntoPensionY, payIntoPensionN, whatFor) ->
  if not checked payIntoPensionY
    S("whatForWrap").slideUp 0

  S(payIntoPensionY).on "click", -> S("whatForWrap").slideDown(0).attr 'aria-hidden', 'false'

  S(payIntoPensionN).on "click", -> S("whatForWrap").slideUp 0, -> S(whatFor).val("")

#
# Pay For things
#

window.whatThings = (payForThingsY, payForThingsN, whatThings) ->
  if not checked(payForThingsY)
    S("whatThingsWrap").slideUp 0, -> val(whatThings,"")
  S(payForThingsY).on "click", -> S("whatThingsWrap").slideDown(0).attr 'aria-hidden', 'false'

  S(payForThingsN).on "click", -> S("whatThingsWrap").slideUp 0, -> val(whatThings,"")


window.whatCosts = (careCostsForThisWorkY, careCostsForThisWorkN, whatCosts) ->
  if not checked careCostsForThisWorkY
    S("whatCostsWrap").slideUp 0

  S(careCostsForThisWorkY).on "click", -> S("whatCostsWrap").slideDown(0).attr 'aria-hidden', 'false'

  S(careCostsForThisWorkN).on "click", -> S("whatCostsWrap").slideUp 0, -> S(whatCosts).val("")