<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<html>
<head>
    <title>PAW Webapp 2026 - Component Gallery</title>
    <link rel="stylesheet" href="<c:url value='/css/legacy-components.css'/>">
    <style>
        .toast {
            border: 1px solid #ccc;
            padding: 10px;
            margin: 10px 0;
            width: 300px;
            border-radius: 4px;
        }
        .toast-success { background-color: #d4edda; border-color: #c3e6cb; color: #155724; }
        .toast-error { background-color: #f8d7da; border-color: #f5c6cb; color: #721c24; }
        .toast-header { font-weight: bold; border-bottom: 1px solid rgba(0,0,0,0.1); margin-bottom: 5px; display: flex; justify-content: space-between; }
        .toast-close { cursor: pointer; border: none; background: none; }

        .btn { padding: 10px 20px; border-radius: 4px; cursor: pointer; border: 1px solid #007bff; background: #007bff; color: white; margin: 5px; }
        .btn-sm { padding: 5px 10px; font-size: 0.8em; }
        .btn-lg { padding: 15px 30px; font-size: 1.2em; }
        .btn:disabled { background: #ccc; border-color: #bbb; cursor: not-allowed; }
        .btn-secondary { background: #6c757d; border-color: #6c757d; }
    </style>
</head>
<body>
    <h1>Component Visualization</h1>

    <p>Message from Controller: <c:out value="${message}"/></p>

    <hr/>

    <h2>Buttons</h2>
    <div>
        <pa:legacy-button text="Small Button" size="sm" />
        <pa:legacy-button text="Default Button" />
        <pa:legacy-button text="Large Button" size="lg" />
        <pa:legacy-button text="Secondary Button" cssClass="btn-secondary" />
        <pa:legacy-button text="Disabled Button" disabled="${true}" />
    </div>

    <hr/>

    <h2>Toasts</h2>
    <div>
        <pa:legacy-toast title="Success Notification"
                  message="The user was created successfully."
                  state="success" />

        <pa:legacy-toast title="Error Occurred"
                  message="Something went wrong while processing your request."
                  state="error" />
    </div>

    <hr/>

    <h2>Car Cards</h2>
    <div>
        <pa:legacy-car-card
            name="Ford Bronco Sport"
            category="Compact SUV"
            imageUrl="https://upload.wikimedia.org/wikipedia/commons/7/71/2021_Ford_Bronco_Sport.jpg"
            rating="4.5"
        />
    </div>

</body>
</html>
