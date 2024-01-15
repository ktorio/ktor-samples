<#-- @ftlvariable name="wishList" type="com.example.model.dao.wishlist.Wishlists" -->
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8"/>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no"/>
    <title>Wishlist</title>
</head>
<body id="mainNav">
<header>
    <div>
        <h3>Wishlist</h3>
        <#list wishList as wish>
            <div>
                <p>
                    <b>
                        ${wish.id}
                    </b>
                    <em>
                        ${wish.wish}
                    </em>
                </p>
            </div>
        </#list>
    </div>
    <div>
        <h4>Make a wish</h4>
        <form action="make" method="post">
            <p>
                <label for="wish">
                    <input type="text" name="wish">
                </label>
            </p>
            <p>
                <input type="submit">
            </p>
        </form>
    </div>
    <div>
        <h4>Cancel a wish</h4>
        <form action="cancel" method="post">
            <p>
                <label for="id">
                    <input type="text" name="id">
                </label>
            </p>
            <p>
                <input type="submit">
            </p>
        </form>
    </div>
</header>
</body>
</html>