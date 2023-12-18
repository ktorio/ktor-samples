<#-- @ftlvariable name="wishList" type="com.example.model.repository.TopWishListExampleRepository" -->
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8"/>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no"/>
    <title>Topwishes</title>
</head>
<body id="mainNav">
<header>
    <div>
        <h3>Top wishes</h3>
        <#list topWishList as topWish>
            <div>
                <p>
                    <b>
                        ${topWish.id}
                    </b>
                    <em>
                        ${topWish.wish}
                    </em>
                </p>
            </div>
        </#list>
    </div>
    <div>
        <a href="/wish/list">To wishlist</a>
    </div>
</header>
</body>
</html>